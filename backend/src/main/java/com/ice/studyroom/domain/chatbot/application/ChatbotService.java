package com.ice.studyroom.domain.chatbot.application;

import com.ice.studyroom.domain.chatbot.domain.category.ChatbotCategory;
import com.ice.studyroom.domain.chatbot.domain.category.ChatbotCategoryRepository;
import com.ice.studyroom.domain.chatbot.domain.exception.ChatbotCategoryNotFoundException;
import com.ice.studyroom.domain.chatbot.domain.exception.ChatbotQuestionNotFoundException;
import com.ice.studyroom.domain.chatbot.domain.question.ChatbotQuestion;
import com.ice.studyroom.domain.chatbot.domain.question.ChatbotQuestionRepository;
import com.ice.studyroom.domain.chatbot.domain.service.AnswerGenerator;
import com.ice.studyroom.domain.chatbot.presentation.dto.request.AnswerRequest;
import com.ice.studyroom.domain.chatbot.presentation.dto.response.AnswerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatbotService {

	private final AnswerGenerator answerGenerator;
	private final ChatbotQuestionRepository questionRepository;
	private final ChatbotCategoryRepository categoryRepository;

	public AnswerResponse getAnswer(AnswerRequest request) {
		ChatbotQuestion question = questionRepository.findByQuestionIdAndCategory_CategoryId(
				request.questionId(), request.categoryId())
			.orElseThrow(() -> new ChatbotQuestionNotFoundException(request.questionId()));

		ChatbotCategory category = categoryRepository.findById(request.categoryId())
			.orElseThrow(() -> new ChatbotCategoryNotFoundException(request.categoryId()));

		return answerGenerator.generate(
			request.categoryId(),
			request.questionId(),
			question.getContent(),
			category.getRoute(),
			category.getNotionUrl()
		);
	}
}
