package com.ice.studyroom.domain.reservation.dto.request;

import java.time.LocalTime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CreateReservationRequest {
	private Long userId;
	private Long[] scheduleId;
	private String userName;
	private String roomNumber;
	private LocalTime startTime;
	private LocalTime endTime;

	public void validateScheduleIds() {
		if (scheduleId == null || scheduleId.length == 0 || scheduleId.length > 2) {
			throw new IllegalArgumentException("예약은 1~2시간만 가능합니다.");
		}
	}

	public boolean isConsecutiveReservation() {
		return scheduleId.length == 2;
	}
}
