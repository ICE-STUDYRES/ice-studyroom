package com.ice.studyroom.domain.reservation.domain.exception.reservation;

import com.ice.studyroom.global.exception.BusinessException;
import com.ice.studyroom.global.type.StatusCode;

public class QrTokenFieldNotFoundException extends BusinessException {
	public QrTokenFieldNotFoundException(String message) {
		super(StatusCode.NOT_FOUND, message);
	}
}
