package com.ice.studyroom.global.exception.token;

import com.ice.studyroom.global.exception.BusinessException;
import com.ice.studyroom.global.type.StatusCode;

public class InvalidQrTokenException extends BusinessException {
	public InvalidQrTokenException(String message) {
		super(StatusCode.NOT_FOUND, message);
	}
}
