package com.ice.studyroom.domain.chatbot.infrastructure.persistence;

import com.ice.studyroom.domain.chatbot.domain.entity.ChatbotEventLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatbotEventLogRepository extends JpaRepository<ChatbotEventLog, Long> {
}
