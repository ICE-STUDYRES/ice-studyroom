package com.ice.studyroom.domain.reservation.presentation.dto.request;

import java.time.LocalTime;

public record CreateReservationRequest(
	Long[] scheduleId,
	String[] participantEmail,
	String roomNumber,
	LocalTime startTime,
	LocalTime endTime
) {
	public CreateReservationRequest {
		// 생성자 내부에서 기본 유효성 검사 수행
		if (scheduleId == null || scheduleId.length == 0 || scheduleId.length > 2) {
			throw new IllegalArgumentException("예약은 1~2시간만 가능합니다.");
		}
	}

	public boolean isConsecutiveReservation() {
		return scheduleId.length == 2;
	}

	public void validateScheduleIds() {
	}
}
