package com.ice.studyroom.domain.chatbot.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.*;

public record AnswerResponse (
	@Schema(description = "카테고리 ID", example = "reservation")
	String categoryId,

	@Schema(description = "질문 ID", example = "1")
	Long questionId,

	@Schema(description = "AI 생성 답변 요약")
	String summary,
	Evidence evidence,
	Links links
)
{
	public record Evidence(
		@Schema(description = "근거 문서 스니펫 목록")
		List<String> snippets
	){}
	public record Links(
		@Schema(description = "관련 페이지 라우트", example = "/reservation")
		String route,

		@Schema(description = "Notion 문서 URL")
		String notionUrl
	){}
}
