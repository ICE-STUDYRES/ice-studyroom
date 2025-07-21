package com.ice.studyroom.domain.reservation.domain.exception.reservation;

import com.ice.studyroom.global.exception.BusinessException;
import com.ice.studyroom.global.type.StatusCode;

/**
 * 유효하지않은 스케줄 ID를 클라이언트에게 전달 받았을 경우 발생시키는 예외입니다.
 */
public class InvalidReservationCreationException extends BusinessException {
	public InvalidReservationCreationException(String message) {
		super(StatusCode.BAD_REQUEST, message);
	}
}
