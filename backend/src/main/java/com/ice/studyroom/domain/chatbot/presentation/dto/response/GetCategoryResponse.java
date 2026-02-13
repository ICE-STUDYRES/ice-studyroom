package com.ice.studyroom.domain.chatbot.presentation.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
public class GetCategoryResponse {

    private List<CategoryItem> categories;

    @Getter
    @Builder
    public static class CategoryItem {
        private final String categoryId;
        private final String label;

        public CategoryItem(String categoryId, String label) {
            this.categoryId = categoryId;
            this.label = label;
        }
    }
}