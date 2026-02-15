package com.ice.studyroom.domain.membership.presentation.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PasswordResetRequest(

	@NotBlank(message = "이메일은 필수입니다.")
	@Email(message = "올바른 이메일 형식이 아닙니다.")
	String email,

	@NotBlank(message = "비밀번호는 필수입니다.")
	@Pattern(
		regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
		message = "비밀번호는 8자 이상, 영문, 숫자, 특수문자를 포함해야 합니다."
	)
	String newPassword,

	@NotBlank(message = "비밀번호 확인은 필수입니다.")
	String newPasswordConfirm
) {
}
