package com.ice.studyroom.domain.texttosql.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class TextToSqlService {

	private final JdbcTemplate jdbcTemplate;
	private final SchemaService schemaService;
	private final SqlValidationService sqlValidationService;
	private final ChatClient chatClient;

	public TextToSqlService(
		JdbcTemplate jdbcTemplate,
		SchemaService schemaService,
		SqlValidationService sqlValidationService,
		AnthropicChatModel chatModel
	) {
		this.jdbcTemplate = jdbcTemplate;
		this.schemaService = schemaService;
		this.sqlValidationService = sqlValidationService;
		this.chatClient = ChatClient.create(chatModel);
	}

	/**
	 * 자연어 질문을 SQL로 변환하고 실행합니다.
	 */
	public QueryResult executeQuery(String userQuery) {
		log.info("Text-to-SQL 요청: {}", userQuery);
		long startTime = System.currentTimeMillis();

		try {
			// 1. LLM으로 SQL 생성
			String sql = generateSQL(userQuery);
			log.info("생성된 SQL: {}", sql);

			// 2. SQL 보안 검증
			sqlValidationService.validate(sql);

			// 3. LIMIT 절 강제 추가
			sql = sqlValidationService.enforceLimitClause(sql);

			// 4. READ-ONLY 계정으로 실행
			List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);

			long executionTime = System.currentTimeMillis() - startTime;
			log.info("쿼리 실행 성공. 결과 행 수: {}, 실행 시간: {}ms", result.size(), executionTime);

			return new QueryResult(sql, result, executionTime);

		} catch (Exception e) {
			log.error("Text-to-SQL 실행 실패: {}", userQuery, e);
			throw e;
		}
	}

	/**
	 * LLM을 사용하여 자연어를 SQL로 변환합니다.
	 */
	private String generateSQL(String userQuery) {
		String promptTemplate = """
            당신은 MySQL 쿼리 전문가입니다.
            다음 데이터베이스 스키마를 참고하여 사용자의 자연어 질문을 SQL로 변환하세요.

            {schema}

            {relationships}

            ## 규칙:
            1. SELECT 문만 생성하세요
            2. 테이블명과 컬럼명을 정확히 사용하세요
            3. MySQL 문법을 따르세요
            4. 날짜 관련 질문은 MySQL 날짜 함수를 사용하세요 (CURDATE(), NOW(), DATE() 등)
            5. JOIN이 필요한 경우 명시적으로 작성하세요
            6. 집계 쿼리는 GROUP BY를 반드시 포함하세요

            ## 예시:
            질문: "오늘 예약 현황"
            SQL: SELECT r.id, m.name, s.schedule_date, s.start_time, r.status
                 FROM reservation r
                 JOIN member m ON r.member_id = m.id
                 JOIN schedule s ON r.schedule_id = s.id
                 WHERE s.schedule_date = CURDATE()

            질문: "이번 주 예약 많은 방 TOP 5"
            SQL: SELECT rts.room_number, COUNT(*) as reservation_count
                 FROM reservation r
                 JOIN schedule s ON r.schedule_id = s.id
                 JOIN room_time_slot rts ON s.room_time_slot_id = rts.id
                 WHERE YEARWEEK(s.schedule_date) = YEARWEEK(NOW())
                 GROUP BY rts.room_number
                 ORDER BY reservation_count DESC
                 LIMIT 5

            사용자 질문: {query}

            SQL만 반환하세요 (설명이나 마크다운 없이):
            """;

		Prompt prompt = new PromptTemplate(promptTemplate)
			.create(Map.of(
				"schema", schemaService.getSchemaInfo(),
				"relationships", schemaService.getRelationshipInfo(),
				"query", userQuery
			));

		String response = chatClient.prompt(prompt)
			.call()
			.content();

		// LLM 응답에서 SQL만 추출 (```sql ``` 제거)
		return cleanSqlResponse(response);
	}

	/**
	 * LLM 응답에서 순수 SQL만 추출합니다.
	 */
	private String cleanSqlResponse(String response) {
		// ```sql ... ``` 형태 제거
		String cleaned = response
			.replaceAll("```sql\\n?", "")
			.replaceAll("```\\n?", "")
			.trim();

		// 세미콜론 제거 (JdbcTemplate에서 자동 처리)
		if (cleaned.endsWith(";")) {
			cleaned = cleaned.substring(0, cleaned.length() - 1);
		}

		return cleaned;
	}

	/**
	 * 쿼리 실행 결과를 담는 내부 클래스
	 */
	public record QueryResult(
		String sql,
		List<Map<String, Object>> data,
		Long executionTimeMs
	) {}
}
