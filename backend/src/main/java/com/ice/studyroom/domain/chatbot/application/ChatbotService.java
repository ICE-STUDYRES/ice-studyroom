package com.ice.studyroom.domain.chatbot.application;

import com.ice.studyroom.domain.chatbot.domain.service.AnswerGenerator;
import com.ice.studyroom.domain.chatbot.presentation.dto.request.AnswerRequest;
import com.ice.studyroom.domain.chatbot.presentation.dto.response.AnswerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatbotService {

	private final AnswerGenerator answerGenerator;

	public AnswerResponse getAnswer(AnswerRequest request) {
		return answerGenerator.generate(
			request.categoryId(),
			request.questionId()
		);
	}
}
