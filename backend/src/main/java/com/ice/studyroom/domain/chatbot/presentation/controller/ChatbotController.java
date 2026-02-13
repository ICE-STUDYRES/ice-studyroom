package com.ice.studyroom.domain.chatbot.presentation.controller;

import com.ice.studyroom.domain.chatbot.application.ChatbotQueryService;
import com.ice.studyroom.domain.chatbot.application.ChatbotService;
import com.ice.studyroom.domain.chatbot.presentation.dto.request.AnswerRequest;
import com.ice.studyroom.domain.chatbot.presentation.dto.response.AnswerResponse;
import com.ice.studyroom.domain.chatbot.presentation.dto.response.GetCategoryQuestionsResponse;
import com.ice.studyroom.domain.chatbot.presentation.dto.response.GetCategoryResponse;
import com.ice.studyroom.global.dto.response.ResponseDto;
import com.ice.studyroom.global.type.StatusCode;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/chatbot")
@Tag(name = "Chatbot", description = "챗봇 API")
public class ChatbotController {

    private final ChatbotQueryService chatbotQueryService;
    private final ChatbotService chatbotService;

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

    // 3. 챗봇 답변 생성/조회
    @Operation(
        summary = "챗봇 답변 생성/조회",
        description = "카테고리와 질문 ID를 받아 AI 기반 답변을 생성합니다."
    )
    @ApiResponse(responseCode = "200", description = "답변 생성 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    @PostMapping("/answers")
    public ResponseEntity<ResponseDto<AnswerResponse>> getAnswer(
        @Valid @RequestBody AnswerRequest request
    ) {
        AnswerResponse response = chatbotService.getAnswer(request);
        return ResponseEntity.ok(ResponseDto.of(response));
    }
}
