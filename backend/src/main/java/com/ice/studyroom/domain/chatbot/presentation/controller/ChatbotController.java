package com.ice.studyroom.domain.chatbot.presentation.controller;

import com.ice.studyroom.domain.chatbot.application.ChatbotService;
import com.ice.studyroom.domain.chatbot.presentation.dto.request.AnswerRequest;
import com.ice.studyroom.domain.chatbot.presentation.dto.response.AnswerResponse;
import com.ice.studyroom.global.dto.response.ResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/chatbot")
public class ChatbotController {
	// 서비스 사용
	private final ChatbotService chatbotService;

	@Operation(
		summary = "챗봇 답변 생성/조회 API",
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
