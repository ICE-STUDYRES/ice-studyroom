package com.ice.studyroom.domain.chatbot.infrastructure.openai.dto;

import java.util.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OpenAiResponseResult {

	private List<OutputItem> output;

	@Getter
	@NoArgsConstructor
	public static class OutputItem {
		private String type;
		private List<ContentItem> content; // type=message 일 때
		private List<SearchResult> results; // type=file_search_call 일 때
	}

	@Getter
	@NoArgsConstructor
	public static class ContentItem {
		private String type;
		private String text;
	}

	@Getter
	@NoArgsConstructor
	public static class SearchResult {
		private String text;
	}
}
