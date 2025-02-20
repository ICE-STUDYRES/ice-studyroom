package com.ice.studyroom.domain.membership.presentation.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record MemberCreateRequest(
	@NotBlank(message = "이메일은 필수입니다.")
	@Email(message = "올바른 이메일 형식이 아닙니다.")
	String email,
	@NotNull(message = "이메일 인증을 진행해주세요.")
	Boolean isAuthenticated,
	@NotBlank(message = "이메일 인증을 진행해주세요.")
	String authenticationCode,
	@NotBlank(message = "비밀번호는 필수입니다.")
	String password,
	@NotBlank(message = "이름은 필수입니다.")
	String name,
	@NotBlank(message = "학번은 필수입니다.")
	@Pattern(regexp = "\\d{9}", message = "학번은 9자리 숫자로 이루어져야 합니다.")
	String studentNum
) {
}
