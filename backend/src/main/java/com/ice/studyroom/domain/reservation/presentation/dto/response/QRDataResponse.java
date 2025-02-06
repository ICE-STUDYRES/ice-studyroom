package com.ice.studyroom.domain.reservation.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class QRDataResponse {
	private Long reservationId;
	private String email;
}
