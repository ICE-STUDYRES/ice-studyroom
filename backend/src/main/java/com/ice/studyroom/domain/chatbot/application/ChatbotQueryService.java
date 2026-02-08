package com.ice.studyroom.domain.chatbot.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ice.studyroom.domain.chatbot.application.dto.GetCategoryResponse;
import com.ice.studyroom.domain.chatbot.application.dto.GetCategoryQuestionsResponse;
import com.ice.studyroom.domain.chatbot.domain.category.ChatbotCategoryRepository;
import com.ice.studyroom.domain.chatbot.domain.question.ChatbotQuestion;
import com.ice.studyroom.domain.chatbot.domain.question.ChatbotQuestionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatbotQueryService {

    private final ChatbotCategoryRepository categoryRepository;
    private final ChatbotQuestionRepository questionRepository;

    public GetCategoryResponse getCategories() {
        return GetCategoryResponse.builder()
            .categories(
                categoryRepository.findAll().stream()
                    .map(c -> GetCategoryResponse.CategoryItem.builder()
                        .categoryId(c.getCategoryId())
                        .label(c.getLabel())
                        .build())
                    .toList()
            )
            .build();
    }

    public GetCategoryQuestionsResponse getCategoryQuestions(String categoryId, boolean includeClickCount) {
        List<ChatbotQuestion> questions =
            questionRepository.findByCategory_CategoryIdOrderByQuestionIdAsc(categoryId);

        return GetCategoryQuestionsResponse.builder()
            .categoryId(categoryId)
            .questions(
                questions.stream()
                    .map(q -> GetCategoryQuestionsResponse.QuestionItem.builder()
                        .questionId(q.getQuestionId())
                        .content(q.getContent())
                        .clickCount(includeClickCount ? q.getClickCount() : null)
                        .build())
                    .toList()
            )
            .build();
    }
}