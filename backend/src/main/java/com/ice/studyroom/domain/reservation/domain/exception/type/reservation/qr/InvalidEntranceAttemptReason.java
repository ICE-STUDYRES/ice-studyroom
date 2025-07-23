package com.ice.studyroom.domain.reservation.domain.exception.type.reservation.qr;

public enum InvalidEntranceAttemptReason {
	ALREADY_USED("이미 입실 처리 된 예약입니다."),
	ALREADY_CANCELLED("취소된 예약입니다."),
	ALREADY_COMPLETED("이미 정상퇴실 처리된 예약입니다."),
	NO_SHOW("이미 노쇼 처리 된 예약입니다."),
	INVALID_STATE("입장 처리를 할 수 없는 상태의 예약입니다.");

	private final String message;

	InvalidEntranceAttemptReason(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
}
