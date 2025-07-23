package com.ice.studyroom.domain.reservation.domain.exception.reservation.qr;

import com.ice.studyroom.domain.reservation.domain.exception.type.qr.QrIssuanceErrorReason;
import com.ice.studyroom.global.exception.BusinessException;
import com.ice.studyroom.global.type.StatusCode;

/**
 * QR 코드 발급 등 특정 작업을 수행하기에 유효하지 않은 예약 상태일 때 발생하는 예외입니다.
 */
public class QrIssuanceNotAllowedException extends BusinessException {
	private final Long reservationId;

	public QrIssuanceNotAllowedException(QrIssuanceErrorReason reason, Long reservationId) {
		super(StatusCode.CONFLICT, reason.getMessage());
		this.reservationId = reservationId;
	}

	public Long getReservationId() {
		return reservationId;
	}
}
