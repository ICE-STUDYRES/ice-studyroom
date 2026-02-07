package com.ice.studyroom.domain.chatbot.presentation.dto.response;

import com.ice.studyroom.domain.chatbot.domain.type.CategoryType;
import java.util.*;

public record AnswerResponse (
	CategoryType categoryId,
	Long questionId,
	String summary,
	Evidence evidence,
	Links links,
	Support support
)
{
	public record Evidence(
		List<String> snippets
	){}
	public record Links(
		String route,
		String notionUrl
	){}
	public record Support(
		String openChatUrl,
		String managerPhone
	){}
}
