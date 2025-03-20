package com.ice.studyroom.domain.admin.presentation.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AdminOccupyRequest (
	@NotEmpty(message = "선점을 원하는 시간대를 선택해주세요.")
	List<Long> roomTimeSlotId
) {
}
