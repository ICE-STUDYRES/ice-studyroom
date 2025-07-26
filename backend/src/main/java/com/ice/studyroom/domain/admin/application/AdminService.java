package com.ice.studyroom.domain.admin.application;

import com.ice.studyroom.domain.admin.domain.service.manage.RoomModificationService;
import com.ice.studyroom.domain.admin.domain.type.DayOfWeekStatus;
import com.ice.studyroom.domain.admin.domain.util.AdminLogUtil;
import com.ice.studyroom.domain.admin.presentation.dto.request.*;
import com.ice.studyroom.domain.admin.presentation.dto.response.*;
import com.ice.studyroom.domain.membership.domain.entity.Member;
import com.ice.studyroom.domain.penalty.application.PenaltyService;
import com.ice.studyroom.domain.penalty.domain.entity.Penalty;
import com.ice.studyroom.domain.membership.infrastructure.persistence.MemberRepository;
import com.ice.studyroom.domain.penalty.infrastructure.persistence.PenaltyRepository;
import com.ice.studyroom.domain.admin.domain.entity.RoomTimeSlot;
import com.ice.studyroom.domain.admin.infrastructure.persistence.RoomTimeSlotRepository;
import com.ice.studyroom.domain.schedule.domain.entity.Schedule;
import com.ice.studyroom.domain.reservation.domain.type.ScheduleSlotStatus;
import com.ice.studyroom.domain.reservation.infrastructure.persistence.ScheduleRepository;
import com.ice.studyroom.global.exception.BusinessException;
import com.ice.studyroom.global.type.StatusCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

	private final RoomTimeSlotRepository roomTimeSlotRepository;
	private final RoomModificationService roomModificationService;
	private final PenaltyRepository penaltyRepository;
	private final ScheduleRepository scheduleRepository;
	private final MemberRepository memberRepository;
	private final PenaltyService penaltyService;

	@Transactional
	public String adminOccupyRooms(AdminOccupyRequest request) {
		AdminLogUtil.log("관리자 선점 요청", "roomTimeSlotIds: " + request.roomTimeSlotId(), "요일: " + request.dayOfWeek());
		//당일 선점일 경우, 스케줄에도 반영
		if(request.dayOfWeek() == DayOfWeekStatus.valueOf(LocalDate.now().getDayOfWeek().name())){
			List<Schedule> todaySchedules = scheduleRepository.findByScheduleDateAndRoomTimeSlotIdIn(LocalDate.now(),
				request.roomTimeSlotId());

			if(todaySchedules.isEmpty()){
				AdminLogUtil.logWarn("선점 실패 - 당일 일치 스케줄 없음", "roomTimeSlotIds: " + request.roomTimeSlotId(), "date: " + LocalDate.now());
				throw new BusinessException(StatusCode.NOT_FOUND, "해당 RoomTimeSlot ID에 일치하는 Schedule이 존재하지 않습니다.");
			}

			for (Schedule schedule : todaySchedules) {
				if(schedule.getStatus() == ScheduleSlotStatus.RESERVED){
					AdminLogUtil.logWarn("선점 실패 - 예약된 스케줄 존재", "scheduleId: " + schedule.getId());
					throw new BusinessException(StatusCode.BAD_REQUEST, "예약이 이루어진 스케줄에 대해서는 선점이 불가능합니다.");
				}
				schedule.updateStatus(ScheduleSlotStatus.UNAVAILABLE);
			}
		}

		List<RoomTimeSlot> roomTimeSlots = roomTimeSlotRepository.findAllById(request.roomTimeSlotId());

		if (roomTimeSlots.isEmpty()) {
			AdminLogUtil.logWarn("선점 실패 - 유효하지 않은 roomTimeSlot ID", "요청 ID 목록: " + request.roomTimeSlotId());
			throw new BusinessException(StatusCode.NOT_FOUND, "해당 ID에 일치하는 RoomTimeSlot이 존재하지 않습니다.");
		}

		for (RoomTimeSlot roomTimeSlot : roomTimeSlots) {
			roomTimeSlot.updateStatus(ScheduleSlotStatus.UNAVAILABLE);
		}
		AdminLogUtil.log("관리자 선점 완료", "선점된 roomTimeSlot 수: " + roomTimeSlots.size());
		return "관리자가 선택한 시간대를 선점했습니다.";
	}

	@Transactional
	public String adminReleaseRooms(AdminReleaseRequest request) {
		AdminLogUtil.log("관리자 선점 해제 요청", "roomTimeSlotIds: " + request.roomTimeSlotId(), "요일: " + request.dayOfWeek());
		//당일 선점 해지일 경우, 스케줄에도 반영
		if(request.dayOfWeek() == DayOfWeekStatus.valueOf(LocalDate.now().getDayOfWeek().name())){
			List<Schedule> todaySchedules = scheduleRepository.findByScheduleDateAndRoomTimeSlotIdIn(LocalDate.now(),
				request.roomTimeSlotId());

			if(todaySchedules.isEmpty()){
				AdminLogUtil.logWarn("선점 해제 실패 - 당일 일치 스케줄 없음", "roomTimeSlotIds: " + request.roomTimeSlotId(), "date: " + LocalDate.now());
				throw new BusinessException(StatusCode.NOT_FOUND, "해당 RoomTimeSlot ID에 일치하는 Schedule이 존재하지 않습니다.");
			}

			for (Schedule schedule : todaySchedules) {
				if(schedule.getStatus() == ScheduleSlotStatus.RESERVED){
					AdminLogUtil.logWarn("선점 해제 실패 - 예약된 스케줄 존재", "scheduleId: " + schedule.getId());
					throw new BusinessException(StatusCode.BAD_REQUEST, "예약이 이루어진 스케줄에 대해서는 선점 해지가 불가능합니다.");
				}
				schedule.updateStatus(ScheduleSlotStatus.AVAILABLE);
			}
		}

		List<RoomTimeSlot> roomTimeSlots = roomTimeSlotRepository.findAllById(request.roomTimeSlotId());

		if (roomTimeSlots.isEmpty()) {
			AdminLogUtil.logWarn("선점 해제 실패 - 유효하지 않은 roomTimeSlot ID", "요청 ID 목록: " + request.roomTimeSlotId());
			throw new BusinessException(StatusCode.NOT_FOUND, "해당 ID에 일치하는 RoomTimeSlot이 존재하지 않습니다.");
		}

		for (RoomTimeSlot roomTimeSlot : roomTimeSlots) {
			roomTimeSlot.updateStatus(ScheduleSlotStatus.AVAILABLE);
		}
		AdminLogUtil.log("관리자 선점 해제 완료", "해제된 roomTimeSlot 수: " + roomTimeSlots.size());
		return "관리자가 선택한 시간대의 선점을 해제했습니다.";
	}

	public List<RoomScheduleInfoDto> getRoomByDayOfWeek(DayOfWeekStatus dayOfWeekStatus) {
		//오늘 요일이면 room-time-slot이 아닌 schedule을 반환
		if(dayOfWeekStatus == DayOfWeekStatus.valueOf(LocalDate.now().getDayOfWeek().name())){
			List<Schedule> todaySchedules = scheduleRepository.findByScheduleDate(LocalDate.now());
			return todaySchedules.stream().map(RoomScheduleInfoDto::from).toList();
		}

		List<RoomTimeSlot> roomTimeSlots = roomTimeSlotRepository.findByDayOfWeek(dayOfWeekStatus);
		return roomTimeSlots.stream().map(RoomScheduleInfoDto::from).toList();
	}

	public List<AdminGetReservedResponse> getOccupyAndReservedRooms() {
		// 선점된 방 엔티티만 가져오기
		List<RoomTimeSlot> occupyRoomTimeSlots = roomTimeSlotRepository.findByStatus(ScheduleSlotStatus.UNAVAILABLE);
		return occupyRoomTimeSlots.stream().map(AdminGetReservedResponse::from).toList();
	}

	@Transactional(readOnly = true)
	public List<AdminPenaltyRecordResponse> adminGetPenaltyRecords() {
		List<Penalty> penaltyList = penaltyRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));

		return penaltyList.stream()
			.map(penalty -> AdminPenaltyRecordResponse.of(penalty.getMember().getName(),
				penalty.getMember().getStudentNum(), penalty.getReason(), penalty.getStatus(), penalty.getCreatedAt(),
				penalty.getPenaltyEnd()))
			.toList();
	}

	@Transactional
	public String adminSetPenalty(AdminSetPenaltyRequest request) {
		AdminLogUtil.log("관리자 패널티 부여 요청", "학번: " + request.studentNum(), "종료일시: " + request.penaltyEndAt());
		Member member = memberRepository.findByStudentNum(request.studentNum())
			.orElseThrow(() -> {
				AdminLogUtil.logWarn("패널티 부여 실패 - 존재하지 않는 학번", "학번: " + request.studentNum());
				return new BusinessException(StatusCode.NOT_FOUND, "해당 학번을 가진 회원은 존재하지 않습니다.");
			});

		if (member.isPenalty()) {
			AdminLogUtil.logWarn("패널티 부여 실패 - 이미 패널티 상태", "학번: " + request.studentNum());
			throw new BusinessException(StatusCode.BAD_REQUEST, "이미 패널티가 부여된 회원입니다. 패널티 해제 후 다시 시도해주세요.");
		}

		if(member.getRoles().contains("ROLE_ADMIN")){
			AdminLogUtil.logWarn("패널티 부여 실패 - 대상이 ADMIN 계정", "학번: " + request.studentNum());
			throw new BusinessException(StatusCode.BAD_REQUEST, "ADMIN 계정에는 패널티 설정을 할 수 없습니다.");
		}

		penaltyService.adminAssignPenalty(member, request.penaltyEndAt());
		AdminLogUtil.log("패널티 부여 완료", "학번: " + request.studentNum(), "사유: ADMIN");
		return "패널티 부여가 완료되었습니다.";
	}

	@Transactional
	public String adminDelPenalty(AdminDelPenaltyRequest request) {
		AdminLogUtil.log("관리자 패널티 해제 요청", "학번: " + request.studentNum());
		Member member = memberRepository.findByStudentNum(request.studentNum())
			.orElseThrow(() -> {
				AdminLogUtil.logWarn("패널티 해제 실패 - 존재하지 않는 학번", "학번: " + request.studentNum());
				return new BusinessException(StatusCode.NOT_FOUND, "해당 학번을 가진 회원은 존재하지 않습니다.");
			});

		if (!member.isPenalty()) {
			AdminLogUtil.logWarn("패널티 해제 실패 - 제재 상태 아님", "학번: " + request.studentNum());
			throw new BusinessException(StatusCode.BAD_REQUEST, "패널티가 부여되지 않은 회원입니다.");
		}

		penaltyService.adminDeletePenalty(member);
		AdminLogUtil.log("패널티 해제 완료", "학번: " + request.studentNum());
		return "패널티 해제가 완료되었습니다.";
	}

	public List<RoomInfoResponse> adminGetRoomInfo() {
		return roomTimeSlotRepository.findDistinctRoomInfo();
	}

	@Transactional
	public String adminModRoomType(String roomNumber, AdminModRoomTypeRequest request) {
		roomModificationService.changeRoomType(roomNumber, request.roomType());
		AdminLogUtil.log("방 상태 변경", "방 번호: " + roomNumber);
		return "방 상태 반환 성공";
	}
}
