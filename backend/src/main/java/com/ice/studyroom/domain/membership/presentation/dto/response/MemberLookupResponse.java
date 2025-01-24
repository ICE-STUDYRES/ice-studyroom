package com.ice.studyroom.domain.membership.presentation.dto.request;

import jakarta.validation.constraints.NotNull;

public record MemberLookupRequest(
	@NotNull(message = "이 요청을 처리하려면 accessToken이 필요합니다.")
	String accessToken
) {
}
