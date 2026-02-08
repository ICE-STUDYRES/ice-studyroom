package com.ice.studyroom.domain.chatbot.domain.question;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatbotQuestionRepository extends JpaRepository<ChatbotQuestion, Long> {
    List<ChatbotQuestion> findByCategory_CategoryIdOrderByQuestionIdAsc(String categoryId);
}