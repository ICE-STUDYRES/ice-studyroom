package com.ice.studyroom.domain.chatbot.domain.service;

import com.ice.studyroom.domain.chatbot.domain.type.CategoryType;
import com.ice.studyroom.domain.chatbot.presentation.dto.response.AnswerResponse;

public interface AnswerGenerator {
	// 일단 메서드 시그니처만 정의
	// category와 questionId를 받아서 답변 생성
	AnswerResponse generate(CategoryType category, Long questionId);
}
