package com.ice.studyroom.domain.chatbot.domain.exception;

import com.ice.studyroom.global.exception.BusinessException;
import com.ice.studyroom.global.type.StatusCode;
import lombok.Getter;

@Getter
public class ChatbotCategoryNotFoundException extends BusinessException {
	private final String categoryId;

	public ChatbotCategoryNotFoundException(String categoryId) {
		super(StatusCode.NOT_FOUND, "카테고리를 찾을 수 없습니다.");
		this.categoryId = categoryId;
	}
}
