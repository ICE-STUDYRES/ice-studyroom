package com.ice.studyroom.domain.reservation.domain.service;

import com.ice.studyroom.domain.membership.domain.vo.Email;
import com.ice.studyroom.domain.reservation.domain.entity.Reservation;
import com.ice.studyroom.domain.schedule.domain.entity.Schedule;
import com.ice.studyroom.domain.reservation.domain.type.ReservationStatus;
import com.ice.studyroom.domain.reservation.infrastructure.persistence.ReservationRepository;
import com.ice.studyroom.domain.reservation.util.ReservationLogUtil;
import com.ice.studyroom.global.exception.BusinessException;
import com.ice.studyroom.global.type.StatusCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReservationValidator {

	private final ReservationRepository reservationRepository;
	private final Clock clock;

	public void validateSchedulesAvailable(List<Schedule> schedules) {
		LocalDateTime now = LocalDateTime.now(clock);

		if (schedules.stream().anyMatch(schedule -> {
			LocalDateTime scheduleStartDateTime = LocalDateTime.of(schedule.getScheduleDate(), schedule.getStartTime());
			return !schedule.isAvailable() ||
				!schedule.isCurrentResLessThanCapacity() ||
				!scheduleStartDateTime.isAfter(now); // 현재 시간보다 이전이면 예외 발생
		})) {
			ReservationLogUtil.logWarn("스케줄 유효성 검증 실패",
				"현재 시간: " + now,
				"대상 스케줄 ID 목록: " + schedules.stream().map(Schedule::getId).toList());
			throw new BusinessException(StatusCode.BAD_REQUEST, "예약이 불가능합니다. 스케줄이 유효하지 않거나 이미 예약이 완료되었습니다.");
		}
	}

	public void checkDuplicateReservation(Email reservationOwnerEmail) {
		Optional<Reservation> recentReservation = reservationRepository.findLatestReservationByMemberEmail(
			reservationOwnerEmail);
		if (recentReservation.isPresent()) {
			ReservationStatus recentStatus = recentReservation.get().getStatus();
			if (recentStatus == ReservationStatus.RESERVED || recentStatus == ReservationStatus.ENTRANCE) {
				ReservationLogUtil.logWarn("중복 예약 시도",
					"userEmail: " + reservationOwnerEmail.getValue() + "현재 상태: " + recentStatus);
				throw new BusinessException(StatusCode.CONFLICT, "현재 예약이 진행 중이므로 새로운 예약을 생성할 수 없습니다.");
			}
		}
	}
}
