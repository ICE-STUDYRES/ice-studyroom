package com.ice.studyroom.domain.reservation.presentation.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;

import com.ice.studyroom.domain.reservation.domain.entity.Reservation;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationResponse {
	private String message;
	private ReservationInfo reservation;

	@Getter
	@Builder
	public static class ReservationInfo {
		private LocalDate scheduleDate;
		private String roomNumber;
		private LocalTime startTime;
		private LocalTime endTime;
	}

	public static ReservationResponse of(Reservation reservation) {
		ReservationInfo reservationInfo = ReservationInfo.builder()
			.scheduleDate(reservation.getScheduleDate())
			.roomNumber(reservation.getRoomNumber())
			.startTime(reservation.getStartTime())
			.endTime(reservation.getEndTime())
			.build();

		return ReservationResponse.builder()
			.message("예약이 성공적으로 완료되었습니다.")
			.reservation(reservationInfo)
			.build();
	}
}
