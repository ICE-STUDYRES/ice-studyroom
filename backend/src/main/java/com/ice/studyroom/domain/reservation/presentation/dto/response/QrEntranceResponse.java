package com.ice.studyroom.domain.reservation.presentation.dto.response;

import com.ice.studyroom.domain.reservation.domain.type.ReservationStatus;

public record QrEntranceResponse(
	ReservationStatus status,
	String userName,
	String userNumber
) {}
