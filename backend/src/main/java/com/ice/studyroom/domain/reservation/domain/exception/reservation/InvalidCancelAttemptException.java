package com.ice.studyroom.domain.reservation.domain.exception.reservation;

import com.ice.studyroom.domain.reservation.domain.exception.type.reservation.cancel.InvalidCancelAttemptReason;
import com.ice.studyroom.global.exception.BusinessException;
import com.ice.studyroom.global.type.StatusCode;
import lombok.Getter;

/**
 * Reservation.status 가 RESERVED 상태인 경우에만 취소가 가능합니다.
 * 위의 경우 취소를 하기 전에 예약 상태를 검증 하는 과정에서 발생하는 예외를 정의했습니다.
 */
@Getter
public class InvalidCancelAttemptException extends BusinessException {
	private final Long reservationId;

	public InvalidCancelAttemptException(InvalidCancelAttemptReason reason, Long reservationId) {
		super(StatusCode.BAD_REQUEST, reason.getMessage());
		this.reservationId = reservationId;
	}
}
