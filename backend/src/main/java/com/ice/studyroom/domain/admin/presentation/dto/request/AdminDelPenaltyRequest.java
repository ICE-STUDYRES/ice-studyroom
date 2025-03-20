package com.ice.studyroom.domain.admin.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AdminDelPenaltyRequest(
	@NotBlank(message = "학번을 필수로 입력해주세요.")
	String studentNum
) {
}
