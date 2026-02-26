package com.ice.studyroom.domain.chatbot.application;

import com.ice.studyroom.domain.chatbot.domain.event.ChatbotEventLog;
import com.ice.studyroom.domain.chatbot.domain.event.ChatbotEventLogRepository;
import com.ice.studyroom.domain.chatbot.presentation.dto.request.ChatbotEventRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatbotEventService {
	private final ChatbotEventLogRepository eventLogRepository;

	public void logEvent(ChatbotEventRequest request){
		ChatbotEventLog eventLog = ChatbotEventLog.builder()
			.eventType(request.eventType())
			.buttonType(request.buttonType())
			.categoryId(request.categoryId())
			.questionId(request.questionId())
			.screen(request.screen())
			.occurredAt(request.occurredAt())
			.build();
		eventLogRepository.save(eventLog);
	}
}
