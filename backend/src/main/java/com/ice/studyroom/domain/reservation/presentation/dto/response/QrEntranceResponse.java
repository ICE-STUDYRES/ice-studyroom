package com.ice.studyroom.domain.reservation.presentation.dto.response;

import com.ice.studyroom.domain.reservation.domain.type.ReservationStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class QrEntranceResponse {
	ReservationStatus status;
	String userName;
	String userNumber;
}
