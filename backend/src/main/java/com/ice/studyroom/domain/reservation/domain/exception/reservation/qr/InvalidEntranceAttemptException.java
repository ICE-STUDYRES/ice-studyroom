package com.ice.studyroom.domain.reservation.domain.exception.reservation.qr;

import com.ice.studyroom.domain.reservation.domain.exception.type.qr.InvalidEntranceAttemptReason;
import com.ice.studyroom.global.exception.BusinessException;
import com.ice.studyroom.global.type.StatusCode;


/**
 * Reservation.status 가 RESERVED 이외의 상태일 경우 QR을 발급 받을 수 없습니다.
 * 위의 경우 QR을 발급받으려고 할 경우 발생하는 예외입니다.
 */
public class InvalidEntranceAttemptException extends BusinessException {
	private final Long reservationId;

	public InvalidEntranceAttemptException(InvalidEntranceAttemptReason reason, Long reservationId) {
		super(StatusCode.BAD_REQUEST, reason.getMessage());
		this.reservationId = reservationId;
	}

	public Long getReservationId() {
		return reservationId;
	}
}
