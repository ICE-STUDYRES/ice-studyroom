package com.ice.studyroom.domain.reservation.application;

import com.ice.studyroom.domain.admin.domain.type.RoomType;
import com.ice.studyroom.domain.membership.domain.vo.Email;
import com.ice.studyroom.domain.reservation.domain.entity.Schedule;
import com.ice.studyroom.domain.reservation.domain.service.ReservationValidator;
import com.ice.studyroom.domain.reservation.domain.type.ScheduleSlotStatus;
import com.ice.studyroom.domain.reservation.infrastructure.persistence.ScheduleRepository;
import com.ice.studyroom.domain.reservation.util.ReservationLogUtil;
import com.ice.studyroom.global.exception.BusinessException;
import com.ice.studyroom.global.type.StatusCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.PessimisticLockException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReservationConcurrencyService {

	private final ScheduleRepository scheduleRepository;
	private final ReservationValidator reservationValidator;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public List<Schedule> processIndividualReservationWithLock(List<Long> scheduleIds){
		try {
			List<Long> sortedScheduleIds = new ArrayList<>(scheduleIds);
			Collections.sort(sortedScheduleIds);

			List<Schedule> lockedSchedules = scheduleRepository.findByIdsWithPessimisticLock(sortedScheduleIds);

			if (lockedSchedules.size() != scheduleIds.size()) {
				ReservationLogUtil.logWarn("개인 예약 실패 - 존재하지 않는 스케줄 포함됨", "스케줄 ID: " + scheduleIds);
				throw new BusinessException(StatusCode.NOT_FOUND, "존재하지 않는 스케줄이 포함되어 있습니다.");
			}

			RoomType roomType = lockedSchedules.get(0).getRoomType();
			if (roomType == RoomType.GROUP) {
				ReservationLogUtil.logWarn("개인 예약 실패 - 그룹 전용 방 예약 시도",
					"방 번호: " + lockedSchedules.get(0).getRoomNumber());
				throw new BusinessException(StatusCode.FORBIDDEN, "해당 방은 단체예약 전용입니다.");
			}

			// 스케줄 업데이트 (currentRes 증가 및 상태 변경)
			for (Schedule schedule : lockedSchedules) {
				if (!schedule.isCurrentResLessThanCapacity()) {
					ReservationLogUtil.logWarn("개인 예약 실패 - 수용 인원 초과", "스케줄 ID: " + schedule.getId());
					throw new BusinessException(StatusCode.BAD_REQUEST, "예약 가능한 자리가 없습니다.");
				}

				schedule.reserve(); // 개인예약은 현재사용인원에서 +1 진행
				if (schedule.getCurrentRes().equals(schedule.getCapacity())) { //예약 후 현재인원 == 방수용인원 경우 RESERVE
					schedule.updateStatus(ScheduleSlotStatus.RESERVED);
				}
			}
			scheduleRepository.saveAll(lockedSchedules);
			return lockedSchedules;
		} catch (PessimisticLockException e) {
			throw new BusinessException(StatusCode.CONFLICT,
				"현재 예약이 집중되고 있습니다. 잠시 후 다시 시도해주세요.");
		}
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public List<Schedule> processGroupReservationWithLock(List<Long> scheduleIds, Set<String> uniqueEmails){
		try{
			List<Long> sortedScheduleIds = new ArrayList<>(scheduleIds);
			Collections.sort(sortedScheduleIds);

			List<Schedule> lockedSchedules = scheduleRepository.findByIdsWithPessimisticLock(sortedScheduleIds);

			if (lockedSchedules.size() != scheduleIds.size()) {
				ReservationLogUtil.logWarn("단체 예약 실패 - 존재하지 않는 스케줄 포함됨", "스케줄 ID: " + scheduleIds);
				throw new BusinessException(StatusCode.NOT_FOUND, "존재하지 않는 스케줄이 포함되어 있습니다.");
			}

			//임계 영역에서 중복 예약 검사
			for (String email : uniqueEmails) {
				reservationValidator.checkDuplicateReservation(Email.of(email));
			}

			reservationValidator.validateSchedulesAvailable(lockedSchedules);

			RoomType roomType = lockedSchedules.get(0).getRoomType();
			if(roomType == RoomType.INDIVIDUAL) {
				ReservationLogUtil.logWarn("단체 예약 실패 - 개인 전용 방 예약 시도", "방 번호: " + lockedSchedules.get(0).getRoomNumber());
				throw new BusinessException(StatusCode.FORBIDDEN, "해당 방은 개인예약 전용입니다.");
			}

			// 최소 예약 인원(minRes) 검사 (예약자 + 참여자 수 체크)
			int totalParticipants = uniqueEmails.size(); // 예약자 + 참여자 수
			int minRes = lockedSchedules.get(0).getMinRes(); // 모든 Group 전용 schedule의 min_res는 2로 동일
			int capacity = lockedSchedules.get(0).getCapacity(); // 같은 방의 schedule은 capacity는 동일
			if (totalParticipants < minRes) {
				ReservationLogUtil.logWarn("단체 예약 실패 - 최소 인원 미달", "최소 인원: " + minRes, "현재 인원: " + totalParticipants);
				throw new BusinessException(StatusCode.BAD_REQUEST,
					"최소 예약 인원 조건을 만족하지 않습니다. (필요 인원: " + minRes + ", 현재 인원: " + totalParticipants + ")");
			}else if (totalParticipants > capacity) {
				ReservationLogUtil.logWarn("단체 예약 실패 - 최대 인원 초과", "최대 수용 인원: " + capacity, "현재 인원: " + totalParticipants);
				throw new BusinessException(StatusCode.BAD_REQUEST,
					"방의 최대 수용 인원을 초과했습니다. (최대 수용 인원: " + capacity + ", 현재 인원: " + totalParticipants + ")");
			}

			for (Schedule schedule : lockedSchedules) {
				schedule.updateGroupCurrentRes(totalParticipants); // 현재 사용 인원을 예약자 + 참여자 숫자로 지정
				schedule.updateStatus(ScheduleSlotStatus.RESERVED);
			}

			return lockedSchedules;
		}catch (PessimisticLockException e) {
			throw new BusinessException(StatusCode.CONFLICT,
				"현재 예약이 집중되고 있습니다. 잠시 후 다시 시도해주세요.");
		}
	}
}
