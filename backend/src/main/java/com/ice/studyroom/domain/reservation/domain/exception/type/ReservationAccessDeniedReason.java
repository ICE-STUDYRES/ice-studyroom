package com.ice.studyroom.domain.reservation.domain.exception.type;

public enum ReservationAccessDeniedReason {
	NOT_OWNER("유효하지 않은 사용자는 해당 예약에 접근할 수 없습니다.");

	private final String message;

	ReservationAccessDeniedReason(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
}
