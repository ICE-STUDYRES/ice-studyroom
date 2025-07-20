package com.ice.studyroom.domain.reservation.domain.exception.reservation;

import com.ice.studyroom.global.exception.BusinessException;
import com.ice.studyroom.global.type.StatusCode;

/**
 * 요청한 Member 의 소유가 아닌 Reservation인 경우에는 접근할 수 없기에 예외 발생
 */
public class ReservationAccessDeniedException extends BusinessException {
	public ReservationAccessDeniedException(String message) {
		super(StatusCode.FORBIDDEN, message);
	}
}
