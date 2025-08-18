package com.ice.studyroom.domain.reservation.domain.exception.type.reservation;

public enum ReservationNotFoundReason {
	NOT_FOUND("존재하지 않는 예약입니다."),
	NOT_FOUND_BY_QR_TOKEN("특정 QR 토큰이 존재하지 않는 예약입니다.");

	private final String message;

	ReservationNotFoundReason(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
}
