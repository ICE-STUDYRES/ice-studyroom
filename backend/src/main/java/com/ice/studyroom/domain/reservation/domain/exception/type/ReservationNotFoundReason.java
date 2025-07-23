package com.ice.studyroom.domain.reservation.domain.exception.type;

public enum ReservationNotFoundReason {
	NOT_FOUND("존재하지 않는 예약입니다.");

	private final String message;

	ReservationNotFoundReason(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
}
