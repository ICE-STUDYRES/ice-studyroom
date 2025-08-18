package com.ice.studyroom.domain.reservation.domain.exception.type.reservation;

import lombok.Getter;

@Getter
public enum ReservationActionType {
	CANCEL_RESERVATION("예약 취소"),
	VIEW_RESERVATION_DETAILS("예약 상세 조회"),
	EXTEND_RESERVATION("예약 연장"),
	ISSUE_QR_CODE("QR 코드 발급"),
	QR_ENTRANCE("QR 입실");

	private final String description;

	ReservationActionType(String description) {
		this.description = description;
	}
}
