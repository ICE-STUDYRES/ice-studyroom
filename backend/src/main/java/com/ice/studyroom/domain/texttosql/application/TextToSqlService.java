package com.ice.studyroom.domain.texttosql.application;

import com.ice.studyroom.domain.texttosql.domain.entity.SqlExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
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
	private final SqlRetryService sqlRetryService;
	private final FewShotExampleService fewShotExampleService;
	private final ChatClient chatClient;

	public TextToSqlService(
		JdbcTemplate jdbcTemplate,
		SchemaService schemaService,
		SqlValidationService sqlValidationService,
		SqlRetryService sqlRetryService,
		FewShotExampleService fewShotExampleService,
		ChatModel chatModel
	) {
		this.jdbcTemplate = jdbcTemplate;
		this.schemaService = schemaService;
		this.sqlValidationService = sqlValidationService;
		this.sqlRetryService = sqlRetryService;
		this.fewShotExampleService = fewShotExampleService;
		this.chatClient = ChatClient.builder(chatModel).build();

	}

	public QueryResult executeQuery(String userQuery) {
		log.info("Text-to-SQL 요청: {}", userQuery);
		long startTime = System.currentTimeMillis();

		try {
			SqlRetryService.RetryResult retryResult = sqlRetryService.executeWithRetry(
				// SQL 생성 로직
				(query) -> {
					String sql = generateSQL(query);
					sqlValidationService.validate(sql);
					String finalSql = sqlValidationService.enforceLimitClause(sql);
					return finalSql;
				},
				// SQL 실행 로직
				(sql) -> {
					List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
					return result;
				},
				userQuery
			);

			long executionTime = System.currentTimeMillis() - startTime;

			if (retryResult.success()) {
				log.info("쿼리 실행 성공. 결과 행 수: {}, 실행 시간: {}ms, 시도 횟수: {}",
				retryResult.data().size(), executionTime, retryResult.attempts());

				return new QueryResult(
					retryResult.sql(),
					retryResult.data(),
					executionTime,
					retryResult.attempts()
				);
			} else {
				log.error("모든 재시도 실패: {}", retryResult.error());
				throw new RuntimeException("SQL 생성 실패: " + retryResult.error());
			}

		} catch (Exception e) {
			log.error("Text-to-SQL 실행 실패: {}", userQuery, e);
			throw e;
		}
	}

	private String generateSQL(String userQuery) {
		try {
			if (fewShotExampleService == null) {
				log.error("FewShotExampleService가 null입니다!");
				throw new IllegalStateException("FewShotExampleService가 주입되지 않았습니다");
			}

			List<SqlExample> examples = fewShotExampleService.findRelevantExamples(userQuery, 3);
			String examplesPrompt = fewShotExampleService.formatExamplesForPrompt(examples);
			String schemaInfo = schemaService.getSchemaInfo();
			String relationshipInfo = schemaService.getRelationshipInfo();

			String promptTemplate = """
                당신은 MySQL 쿼리 전문가입니다.
                다음 데이터베이스 스키마를 참고하여 사용자의 자연어 질문을 SQL로 변환하세요.

                %s

                %s

                %s

                ## 규칙:
                1. SELECT 문만 생성하세요
                2. 위 예제들을 참고하여 비슷한 패턴을 사용하세요
                3. 테이블명과 컬럼명을 정확히 사용하세요
                4. MySQL 문법을 따르세요
                5. 날짜 관련 질문은 MySQL 날짜 함수를 사용하세요
                6. JOIN이 필요한 경우 명시적으로 작성하세요
                7. 집계 쿼리는 GROUP BY를 반드시 포함하세요

                사용자 질문: %s

                SQL만 반환하세요 (설명이나 마크다운 없이):
                """.formatted(
				schemaInfo,
				relationshipInfo,
				examplesPrompt,
				userQuery
			);


			String response = chatClient.prompt()
				.user(promptTemplate)
				.call()
				.content();

			String cleanedSql = cleanSqlResponse(response);

			return cleanedSql;

		} catch (Exception e) {
			log.error("generateSQL 중 예외 발생", e);
			throw new RuntimeException("SQL 생성 중 오류: " + e.getClass().getSimpleName(), e);
		}
	}

	private String cleanSqlResponse(String response) {
		if (response == null) {
			log.warn("LLM 응답이 null입니다");
			return "";
		}

		String cleaned = response
			.replaceAll("```sql\\n?", "")
			.replaceAll("```\\n?", "")
			.trim();

		if (cleaned.endsWith(";")) {
			cleaned = cleaned.substring(0, cleaned.length() - 1);
		}

		return cleaned;
	}

	public record QueryResult(
		String sql,
		List<Map<String, Object>> data,
		Long executionTimeMs,
		int attempts
	) {}
}
