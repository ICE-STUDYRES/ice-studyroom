package com.ice.studyroom.domain.chatbot.application.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetCategoryQuestionsResponse {

    private String categoryId;
    private List<QuestionItem> questions;

    @Getter
    @Builder
    public static class QuestionItem {
        private Long questionId;
        private String content;

        // includeClickCount=true 일 때만 내려줄 용도
        private Integer clickCount;
    }
}