package com.ice.studyroom.domain.reservation.domain.exception.type.reservation.qr;

public enum InvalidEntranceTimeReason {
	TOO_EARLY("출석 시간이 아닙니다."),
	TOO_LATE("출석 시간이 만료되었습니다.");

	private final String message;

	InvalidEntranceTimeReason(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
}
