package com.ice.studyroom.domain.reservation.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record QrEntranceRequest(
	@NotBlank(message = "QR Token은 필수입니다.")
	String qrToken
) {
}
