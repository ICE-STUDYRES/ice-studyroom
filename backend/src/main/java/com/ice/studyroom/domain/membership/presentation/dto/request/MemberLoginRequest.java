package com.ice.studyroom.domain.membership.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record MemberLoginRequest(
	@NotBlank(message = "이메일은 필수입니다.")
	@Schema(description = "아이디", example = "glaxyt@naver.com")
	@Email(message = "올바른 이메일 형식이 아닙니다.")
	String email,
	@Schema(description = "비밀번호", example = "p1a2s3s4Word")
	@NotBlank(message = "비밀번호는 필수입니다.")
	String password
) {
}
