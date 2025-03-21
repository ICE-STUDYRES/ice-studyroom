package com.ice.studyroom.domain.admin.presentation.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

import com.ice.studyroom.domain.admin.domain.type.DayOfWeekStatus;

public record AdminOccupyRequest (
	@NotEmpty(message = "선점을 원하는 시간대를 선택해주세요.")
	List<Long> roomTimeSlotId,

	@NotNull(message = "선점하고자하는 날짜의 요일을 포함시켜주세요.")
	DayOfWeekStatus dayOfWeek
) {
}
