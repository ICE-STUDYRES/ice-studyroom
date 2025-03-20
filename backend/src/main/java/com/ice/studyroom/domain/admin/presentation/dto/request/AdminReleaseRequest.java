package com.ice.studyroom.domain.admin.presentation.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

public record AdminReleaseRequest(
	@NotEmpty(message = "선점 해지를 원하는 시간대를 선택해주세요.")
	List<Long> roomTimeSlotId
) {
}
