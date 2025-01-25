package com.ice.studyroom.domain.membership.presentation.dto.request;

import jakarta.validation.constraints.NotNull;

public record TokenRequest(
	@NotNull(message = "Refresh Token은 필수입니다.")
	String refreshToken
) {
}
