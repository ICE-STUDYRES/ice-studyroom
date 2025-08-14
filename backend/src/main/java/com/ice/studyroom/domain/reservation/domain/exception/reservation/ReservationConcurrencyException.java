package com.ice.studyroom.domain.reservation.domain.exception.reservation;

import com.ice.studyroom.global.exception.BusinessException;
import com.ice.studyroom.global.type.StatusCode;

public class ReservationConcurrencyException extends BusinessException {
	public ReservationConcurrencyException() {
		super(StatusCode.CONFLICT, "현재 예약이 집중되고 있습니다. 잠시 후 다시 시도해주세요.");
	}
}
