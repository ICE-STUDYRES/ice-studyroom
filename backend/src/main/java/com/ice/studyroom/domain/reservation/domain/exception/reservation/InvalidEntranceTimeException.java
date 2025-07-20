package com.ice.studyroom.domain.reservation.domain.exception.reservation;

import com.ice.studyroom.global.exception.BusinessException;
import com.ice.studyroom.global.type.StatusCode;

/**
 * 출석 처리를 시도할 경우 Reservation.status 가 RESERVED 가 아닌 경우 발생 시키는 예외입니다.
 */
public class InvalidEntranceTimeException extends BusinessException {
	public InvalidEntranceTimeException(String message) {
		super(StatusCode.BAD_REQUEST, message);
	}
}
