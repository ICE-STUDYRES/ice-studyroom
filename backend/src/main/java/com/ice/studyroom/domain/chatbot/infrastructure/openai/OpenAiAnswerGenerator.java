package com.ice.studyroom.domain.chatbot.infrastructure.openai;

import com.ice.studyroom.domain.chatbot.domain.exception.OpenAiApiException;
import com.ice.studyroom.domain.chatbot.domain.service.AnswerGenerator;
import com.ice.studyroom.domain.chatbot.infrastructure.openai.dto.OpenAiResponseRequest;
import com.ice.studyroom.domain.chatbot.infrastructure.openai.dto.OpenAiResponseResult;
import com.ice.studyroom.domain.chatbot.presentation.dto.response.AnswerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OpenAiAnswerGenerator implements AnswerGenerator {
	private final RestTemplate restTemplate;

	@Value("${openai.api-key}")
	private String apiKey;
	@Value("${openai.vector-store-id}")
	private String vectorStoreId;
	@Value("${openai.model}")
	private String model;
	@Value("${openai.responses-url}")
	private String responseUrl;

	public AnswerResponse generate(String categoryId, Long questionId, String questionContent, String route, String notionUrl) {
		// 1. 요청 구성
		OpenAiResponseRequest requestBody = OpenAiResponseRequest.builder()
			.model(model)
			.instructions("당신은 ICE 스터디룸 정책 전문가입니다. 제공된 문서를 바탕으로 한국어로 간결하게 답변하세요.")
			.input(questionContent)
			.tools(List.of(
				OpenAiResponseRequest.Tool.builder()
					.type("file_search")
					.vectorStoreIds(List.of(vectorStoreId))
					.build()
			))
			.include(List.of("file_search_call.results"))
			.build();

		// 2. 헤더 구성
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(apiKey);
		headers.setContentType(MediaType.APPLICATION_JSON);

		// 3. API 호출
		HttpEntity<OpenAiResponseRequest> entity = new HttpEntity<>(requestBody, headers);
		OpenAiResponseResult result;
		try {
			result = restTemplate.postForObject(responseUrl, entity, OpenAiResponseResult.class);
		} catch (HttpClientErrorException e) {
			throw new OpenAiApiException("OpenAI API 호출 실패: " + e.getStatusCode());
		}

		// 4. 응답 파싱
		String summary = extractSummary(result);
		List<String> snippets = extractSnippets(result, questionContent);

		return new AnswerResponse(
			categoryId,
			questionId,
			summary,
			new AnswerResponse.Evidence(snippets),
			new AnswerResponse.Links(route, notionUrl)
		);
	}

	private String extractSummary(OpenAiResponseResult result) {
		return result.getOutput().stream()
			.filter(item -> "message".equals(item.getType()))
			.filter(item -> item.getContent() != null)
			.flatMap(item -> item.getContent().stream())
			.filter(content -> "output_text".equals(content.getType()))
			.map(OpenAiResponseResult.ContentItem::getText)
			.findFirst()
			.orElse("답변을 생성할 수 없습니다.");
	}

	private List<String> extractSnippets(OpenAiResponseResult result, String questionContent) {
		return result.getOutput().stream()
			.filter(item -> "file_search_call".equals(item.getType()))
			.filter(item -> item.getResults() != null)
			.flatMap(item -> item.getResults().stream())
			.map(r -> extractRelevantPart(r.getText(), questionContent))
			.filter(s -> !s.isEmpty())
			.collect(Collectors.toList());
	}

	private String extractRelevantPart(String text, String questionContent) {
		if (text == null) return "";

		// 1. "질문:" 기준으로 Q&A 섹션 분리
		String[] parts = text.split("(?=질문:)");

		// 2. 질문 키워드 추출
		String[] keywords = questionContent.replaceAll("[^가-힣a-zA-Z0-9\\s]", "").split("\\s+");

		// 3. 키워드 매칭 점수가 가장 높은 섹션 선택
		String bestPart = "";
		int bestScore = 0;

		for (String part : parts) {
			int score = 0;
			for (String keyword : keywords) {
				if (keyword.length() > 1 && part.contains(keyword)) {
					score++;
				}
			}
			if (score > bestScore) {
				bestScore = score;
				bestPart = part;
			}
		}

		String relevant = bestPart.isEmpty() ? text : bestPart;
		return relevant.trim().length() <= 300 ? relevant.trim() : relevant.trim().substring(0, 300) + "...";
	}
}
