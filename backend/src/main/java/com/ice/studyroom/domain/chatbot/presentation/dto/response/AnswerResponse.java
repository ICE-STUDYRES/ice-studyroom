package com.ice.studyroom.domain.chatbot.presentation.dto.response;

import java.util.*;

public record AnswerResponse (
	String categoryId,
	Long questionId,
	String summary,
	Evidence evidence,
	Links links
)
{
	public record Evidence(
		List<String> snippets
	){}
	public record Links(
		String route,
		String notionUrl
	){}
}
