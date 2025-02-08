package com.ice.studyroom.domain.admin.presentation.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminPenaltyRequest(
	@NotBlank(message = "이메일은 필수입니다.")
	@Email(message = "올바른 이메일 형식이 아닙니다.")
	String email,
	@NotNull(message = "패널티 부여 여부를 선택해주세요. 부여 : true, 해제 : false")
	boolean setPenalty
) {
}
