package com.ice.studyroom.domain.reservation.presentation.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;

import com.ice.studyroom.domain.reservation.domain.entity.Reservation;
import com.ice.studyroom.domain.reservation.domain.type.ReservationStatus;

public record GetMostRecentReservationResponse(
	LocalDate scheduleDate,
	String roomNumber,
	LocalTime startTime,
	LocalTime endTime
) {
	public static GetMostRecentReservationResponse from(Reservation reservation) {
		return new GetMostRecentReservationResponse(
			reservation.getScheduleDate(),
			reservation.getRoomNumber(),
			reservation.getStartTime(),
			reservation.getEndTime()
		);
	}
}
