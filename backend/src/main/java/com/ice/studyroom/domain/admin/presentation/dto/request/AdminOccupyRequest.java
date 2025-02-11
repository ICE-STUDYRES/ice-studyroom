package com.ice.studyroom.domain.admin.presentation.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AdminOccupyRequest (
	@NotEmpty(message = "선점을 원하는 (방번호, 시작시간, 요일)의 id를 입력해주세요.")
	List<Long> roomTimeSlotId,

	@NotNull(message = "선점 생성/ 취소 여부를 입력해주세요: 생성(true), 해제(false)")
	boolean setOccupy
) {
}
