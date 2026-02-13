package com.ice.studyroom.domain.chatbot.domain.question;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatbotQuestionRepository extends JpaRepository<ChatbotQuestion, Long> {

    @Query("""
        SELECT q
        FROM ChatbotQuestion q
        WHERE q.category.categoryId = :categoryId
        ORDER BY q.questionId ASC
    """)
    List<ChatbotQuestion> findQuestionsByCategoryId(@Param("categoryId") String categoryId);
}