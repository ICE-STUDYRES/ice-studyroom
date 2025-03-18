package com.ice.studyroom.domain.admin.presentation.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AdminDelPenaltyRequest(
	@NotBlank(message = "이메일은 필수로 입력해주세요.")
	@Email(message = "올바른 이메일 형식이 아닙니다.")
	String email
) {

}
