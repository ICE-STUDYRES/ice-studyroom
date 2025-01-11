package com.ice.studyroom.domain.membership.presentation.dto.request;

import jakarta.validation.constraints.NotNull;

public record TokenRequest(
	@NotNull(message = "Access Token은 필수입니다.")
	String accessToken,
	@NotNull(message = "Refresh Token은 필수입니다.")
	String refreshToken
) {
}
