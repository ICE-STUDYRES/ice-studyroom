package com.ice.studyroom.domain.admin.presentation.dto.request;

import jakarta.validation.constraints.NotNull;

public record AdminCreateOccupyRequest(
	@NotNull(message = "선점 (생성/취소) 하고 싶은 날짜, 시간, 방번호의 번호를 선택해주세요.")
	Long roomTimeSlotId
) {
}
