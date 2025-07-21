package com.ice.studyroom.domain.reservation.domain.exception.type;

public enum QrIssuanceErrorReason {
	ALREADY_ENTRANCE("이미 입실 처리된 예약입니다."),
	RESERVATION_CANCELLED("이미 취소된 예약입니다."),
	ALREADY_COMPLETED("이미 정상 퇴실된 예약입니다."),
	NO_SHOW("노쇼 처리된 예약입니다."),
	INVALID_STATE("QR 코드를 발급할 수 없는 예약 상태입니다.");

	private final String message;

	QrIssuanceErrorReason(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
}
