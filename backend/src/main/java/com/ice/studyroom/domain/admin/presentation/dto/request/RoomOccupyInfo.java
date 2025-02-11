package com.ice.studyroom.domain.admin.presentation.dto.request;

import com.ice.studyroom.domain.admin.domain.type.DayOfWeekStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record RoomOccupyInfo (
	@NotNull(message = "방 번호를 입력해주세요")
	String roomNumber,

	@NotNull(message = "시작 시간을 입력해주세요")
	LocalTime startTime,

	@NotNull(message = "요일을 입력해주세요")
	DayOfWeekStatus dayOfWeek
) {
}
