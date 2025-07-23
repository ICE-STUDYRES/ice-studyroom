package com.ice.studyroom.domain.reservation.domain.exception.type.reservation.cancel;

public enum InvalidCancelAttemptReason {
	ALREADY_USED("이미 사용되었거나 완료된 예약은 취소할 수 없습니다."),
	ALREADY_CANCELLED("이미 취소된 예약입니다."),
	TOO_LATE("입실 시간이 초과하였기에 취소할 수 없습니다."),
	INVALID_STATE("취소 할 수 없는 예약 상태입니다.");

	private final String message;

	InvalidCancelAttemptReason(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
}

