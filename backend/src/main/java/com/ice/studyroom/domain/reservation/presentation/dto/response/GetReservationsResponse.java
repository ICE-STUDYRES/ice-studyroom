package com.ice.studyroom.domain.reservation.presentation.dto.response;

import com.ice.studyroom.domain.reservation.domain.entity.Reservation;
import java.util.List;

public record GetReservationsResponse(Reservation reservation, List<ParticipantResponse> participants) {
	public static GetReservationsResponse from(Reservation reservation, List<ParticipantResponse> participants) {
		return new GetReservationsResponse(reservation, participants);
	}
}

