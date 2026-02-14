package com.ice.studyroom.domain.chatbot.domain.event;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatbotEventLogRepository extends JpaRepository<ChatbotEventLog, Long> {
}
