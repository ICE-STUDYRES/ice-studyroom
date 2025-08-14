package com.ice.studyroom.domain.schedule.domain.exception.schedule;

import com.ice.studyroom.global.exception.BusinessException;
import com.ice.studyroom.global.type.StatusCode;

public class ReservationCapacityExceededException extends BusinessException {
	public ReservationCapacityExceededException(String message) {
		super(StatusCode.BAD_REQUEST, message);
	}
}
