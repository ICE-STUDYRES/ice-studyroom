package com.ice.studyroom.domain.chatbot.domain.exception;

import com.ice.studyroom.global.exception.BusinessException;
import com.ice.studyroom.global.type.StatusCode;
import lombok.Getter;

@Getter
public class OpenAiApiException extends BusinessException {
	public OpenAiApiException(String message) {
		super(StatusCode.SERVICE_UNAVAILABLE, message);
	}
}
