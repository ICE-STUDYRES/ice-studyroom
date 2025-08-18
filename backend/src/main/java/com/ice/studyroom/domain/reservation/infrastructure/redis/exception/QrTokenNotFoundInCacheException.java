package com.ice.studyroom.domain.reservation.infrastructure.redis.exception;

import com.ice.studyroom.global.exception.BusinessException;
import com.ice.studyroom.global.type.StatusCode;

public class QrTokenNotFoundInCacheException extends BusinessException {
	public QrTokenNotFoundInCacheException(String message) {
		super(StatusCode.NOT_FOUND, message);
	}
}
