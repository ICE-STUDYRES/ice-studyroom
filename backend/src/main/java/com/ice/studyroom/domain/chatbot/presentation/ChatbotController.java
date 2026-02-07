package com.ice.studyroom.domain.chatbot.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ice.studyroom.domain.chatbot.application.ChatbotQueryService;
import com.ice.studyroom.domain.chatbot.application.dto.GetCategoryResponse;
import com.ice.studyroom.domain.chatbot.application.dto.GetCategoryQuestionsResponse;
import com.ice.studyroom.global.dto.response.ResponseDto;
import com.ice.studyroom.global.type.StatusCode;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/chatbot")
public class ChatbotController {

    private final ChatbotQueryService chatbotQueryService;

    // 1. 카테고리 목록조회
    @GetMapping("/categories")
    public ResponseEntity<ResponseDto<GetCategoryResponse>> getCategories() {
        return ResponseEntity
            .status(StatusCode.OK.getStatus())
            .body(ResponseDto.of(chatbotQueryService.getCategories()));
    }

    // 2. 카테고리 별 대표질문 목록조회
    @GetMapping("/categories/{categoryId}/questions")
    public ResponseEntity<ResponseDto<GetCategoryQuestionsResponse>> getCategoryQuestions(
        @PathVariable String categoryId,
        @RequestParam(name = "includeClickCount", defaultValue = "false") boolean includeClickCount
    ) {
        return ResponseEntity
            .status(StatusCode.OK.getStatus())
            .body(ResponseDto.of(chatbotQueryService.getCategoryQuestions(categoryId, includeClickCount)));
    }
}