package com.ice.studyroom.domain.membership.presentation.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record MemberEmailVerificationRequest(
	@NotBlank(message = "이메일은 필수입니다.")
	@Email(message = "올바른 이메일 형식이 아닙니다.")
	String email,
	@NotBlank(message = "이메일로 발송된 인증코드 6자리를 입력해주세요.")
	String code
) {
}
