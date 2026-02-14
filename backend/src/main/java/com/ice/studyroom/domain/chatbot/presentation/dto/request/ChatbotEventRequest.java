package com.ice.studyroom.domain.chatbot.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ice.studyroom.domain.chatbot.domain.event.type.ChatbotButtonType;
import com.ice.studyroom.domain.chatbot.domain.event.type.ChatbotEventType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record ChatbotEventRequest (

	@NotNull
	@Schema(description = "이벤트 타입", example = "BUTTON_CLICK")
	ChatbotEventType eventType,

	@NotNull
	@Schema(description = "카테고리 ID", example = "ROOM")
	String categoryId,

	@Schema(description = "질문 ID (QUESTION_CLICK, BUTTON_CLICK 시 필수)", example = "1")
	Long questionId,

	@Schema(description = "버튼 타입 (BUTTON_CLICK 시 필수)", example = "EVIDENCE")
	ChatbotButtonType buttonType,

	@Schema(description = "현재 화면", example = "chatbot")
	String screen,

	@NotNull
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	@Schema(description = "이벤트 발생 시각", example = "2025-01-01T12:00:00")
	LocalDateTime occurredAt
) {}
