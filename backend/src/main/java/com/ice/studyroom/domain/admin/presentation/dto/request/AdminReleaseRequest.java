package com.ice.studyroom.domain.admin.presentation.dto.request;

import java.util.List;

import com.ice.studyroom.domain.admin.domain.type.DayOfWeekStatus;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record AdminReleaseRequest(
	@NotEmpty(message = "선점 해지를 원하는 시간대를 선택해주세요.")
	List<Long> roomTimeSlotId,

	@NotNull(message = "선점을 해지하고자 하는 날짜의 요일을 포함시켜주세요.")
	DayOfWeekStatus dayOfWeek
) {
}
