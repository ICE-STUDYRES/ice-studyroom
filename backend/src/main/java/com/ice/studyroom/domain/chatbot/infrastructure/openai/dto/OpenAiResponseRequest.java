package com.ice.studyroom.domain.chatbot.infrastructure.openai.dto;

import java.util.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OpenAiResponseRequest {
	private String model;
	private String instructions;
	private String input;
	private List<Tool> tools;

	@Getter
	@Builder
	public static class Tool {
		private String type;

		@JsonProperty("vector_store_ids")
		private List<String> vectorStoreIds;
	}
}
