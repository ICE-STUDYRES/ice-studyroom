package com.ice.studyroom.domain.reservation.presentation.dto.response;

import java.util.List;

import com.ice.studyroom.domain.reservation.domain.entity.Reservation;

public record GetReservationsResponse(Reservation reservation, List<ParticipantResponse> participants) {
	public static GetReservationsResponse from(Reservation reservation, List<ParticipantResponse> participants) {
		return new GetReservationsResponse(reservation, participants);
	}
}

