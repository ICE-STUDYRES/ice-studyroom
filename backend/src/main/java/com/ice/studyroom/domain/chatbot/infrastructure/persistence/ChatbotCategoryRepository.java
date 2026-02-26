package com.ice.studyroom.domain.chatbot.infrastructure.persistence;

import com.ice.studyroom.domain.chatbot.domain.entity.ChatbotCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatbotCategoryRepository extends JpaRepository<ChatbotCategory, String> {
}
