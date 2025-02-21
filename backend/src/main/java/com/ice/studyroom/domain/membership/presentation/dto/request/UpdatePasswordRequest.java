package com.ice.studyroom.domain.membership.presentation.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdatePasswordRequest(
	@NotNull(message = "기존 비밀번호는 필수입니다.")
	String currentPassword,

	//@Pattern(regexp = "^(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[a-z\\d@$!%*?&]{8,}$", message = "비밀번호는 최소 8자 이상, 하나 이상의 소문자, 숫자 및 특수 문자를 포함해야 합니다.")
	@NotNull(message = "새로운 비밀번호는 필수입니다.")
	String updatedPassword,

	@NotNull(message = "확인용 새로운 비밀번호는 필수입니다.")
	String updatedPasswordForCheck
) {
}
