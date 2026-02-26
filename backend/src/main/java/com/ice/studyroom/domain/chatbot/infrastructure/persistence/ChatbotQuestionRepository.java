package com.ice.studyroom.domain.chatbot.infrastructure.persistence;

import java.util.List;

import com.ice.studyroom.domain.chatbot.domain.entity.ChatbotQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface ChatbotQuestionRepository extends JpaRepository<ChatbotQuestion, Long> {

    @Query("""
        SELECT q
        FROM ChatbotQuestion q
        WHERE q.category.id = :categoryId
        ORDER BY q.id ASC
    """)
    List<ChatbotQuestion> findQuestionsByCategoryId(@Param("categoryId") String categoryId);

	Optional<ChatbotQuestion> findByIdAndCategory_Id(Long questionId, String categoryId);

}
