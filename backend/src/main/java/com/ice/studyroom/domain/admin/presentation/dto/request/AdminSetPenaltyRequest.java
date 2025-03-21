package com.ice.studyroom.domain.admin.presentation.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminSetPenaltyRequest(
	@NotBlank(message = "학번은 필수로 입력해주세요.")
	String studentNum,

	@NotNull(message = "페널티 종료일자는 필수로 입력해주세요.")
	@FutureOrPresent(message = "페널티 종료일자는 현재 또는 미래 시점이어야 합니다.")
	LocalDateTime penaltyEndAt
) {
}
