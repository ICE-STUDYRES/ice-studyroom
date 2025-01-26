package com.ice.studyroom.domain.membership.presentation.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdatePasswordRequest(
	@NotNull(message = "기존 비밀번호는 필수입니다.")
	String currentPassword,

	@NotNull(message = "새로운 비밀번호는 필수입니다.")
	String updatedPassword,

	@NotNull(message = "확인용 새로운 비밀번호는 필수입니다.")
	String updatedPasswordForCheck
) {
}
