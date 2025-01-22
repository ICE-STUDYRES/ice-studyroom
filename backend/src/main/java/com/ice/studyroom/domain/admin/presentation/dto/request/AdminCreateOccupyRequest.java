package com.ice.studyroom.domain.admin.presentation.dto.request;

import jakarta.validation.constraints.NotNull;

public record AdminCreateOccupyRequest(
//	@NotNull(message = "방번호는 필수입니다")
//	String roomNumber,
//	@NotNull(message = "시작시간은 필수입니다.")
//	LocalTime startTime,
//	@NotNull(message = "종료시간은 필수입니다.")
//	LocalTime endTime,
//	@NotNull(message = "요일은 필수입니다.")
//	DayOfWeekStatus dayOfWeek
//	) {

	@NotNull(message = "예약하고 싶은 날짜, 시간, 방번호의 번호를 선택해주세요.")
	Long roomTimeSlotId
) {
}
