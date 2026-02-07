package com.ice.studyroom.domain.chatbot.presentation.controller;

import com.ice.studyroom.domain.chatbot.application.ChatbotService;
import com.ice.studyroom.domain.chatbot.presentation.dto.request.AnswerRequest;
import com.ice.studyroom.domain.chatbot.presentation.dto.response.AnswerResponse;
import com.ice.studyroom.global.dto.response.ResponseDto;
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

	@PostMapping("/answers")
	public ResponseEntity<ResponseDto<AnswerResponse>> getAnswer(
		@Valid @RequestBody AnswerRequest request
	) {
		AnswerResponse response = chatbotService.getAnswer(request);
		return ResponseEntity.ok(ResponseDto.of(response));
	}

}
