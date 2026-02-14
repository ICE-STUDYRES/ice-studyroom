package com.ice.studyroom.domain.chatbot.domain.exception;

import com.ice.studyroom.global.exception.BusinessException;
import com.ice.studyroom.global.type.StatusCode;
import lombok.Getter;

@Getter
public class ChatbotQuestionNotFoundException extends BusinessException {
	private final Long questionId;

	public ChatbotQuestionNotFoundException(Long questionId) {
		super(StatusCode.NOT_FOUND, "질문을 찾을 수 없습니다");
		this.questionId = questionId;
	}
}
