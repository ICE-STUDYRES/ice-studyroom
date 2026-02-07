package com.ice.studyroom.domain.chatbot.application.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetCategoryResponse {

    private List<CategoryItem> categories;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryItem {
        private String categoryId;
        private String label;
    }
}