package com.ice.studyroom.domain.chatbot.domain.service;

import com.ice.studyroom.domain.chatbot.presentation.dto.response.AnswerResponse;

public interface AnswerGenerator {
	AnswerResponse generate(String category, Long questionId, String questionContent, String route, String notionUrl);
}
