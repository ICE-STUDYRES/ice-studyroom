package com.ice.studyroom.domain.chatbot.presentation.dto.request;

import jakarta.validation.constraints.NotNull;

// 1. 불변 DTO (record 사용)
public record AnswerRequest (
	// 2. 필드
	@NotNull
	String categoryId,
	// 2-1. NotNull 어노테이션 : null이면 해당 요청은 유효 X
	@NotNull
	Long questionId
) {}
