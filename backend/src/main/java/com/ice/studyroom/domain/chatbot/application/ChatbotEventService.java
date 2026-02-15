package com.ice.studyroom.domain.chatbot.application;

import com.ice.studyroom.domain.chatbot.domain.event.ChatbotEventLog;
import com.ice.studyroom.domain.chatbot.domain.event.ChatbotEventLogRepository;
import com.ice.studyroom.domain.chatbot.presentation.dto.request.ChatbotEventRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatbotEventService {
	private final ChatbotEventLogRepository eventLogRepository;

	@Transactional
	public void logEvent(ChatbotEventRequest request){
		ChatbotEventLog eventLog = new ChatbotEventLog(
			request.eventType(),
			request.buttonType(),
			request.categoryId(),
			request.questionId(),
			request.screen(),
			request.occurredAt()
		);
		eventLogRepository.save(eventLog);
	}
}
