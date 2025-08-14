package com.ice.studyroom.global.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseMessage {
	INDIVIDUAL_RESERVATION_SUCCESS("성공적으로 개인 예약을 완료했습니다.");

	private final String message;
}
