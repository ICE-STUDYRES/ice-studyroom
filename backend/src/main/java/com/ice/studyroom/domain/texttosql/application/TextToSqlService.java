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

		// 🆕 초기화 확인 로깅
		log.info("TextToSqlService 초기화 완료 - FewShotExampleService: {}",
			fewShotExampleService != null ? "주입됨" : "NULL!");
	}

	public QueryResult executeQuery(String userQuery) {
		log.info("Text-to-SQL 요청: {}", userQuery);
		long startTime = System.currentTimeMillis();

		try {
			SqlRetryService.RetryResult retryResult = sqlRetryService.executeWithRetry(
				// SQL 생성 로직
				(query) -> {
					log.debug("generateSQL 호출 시작: {}", query);
					String sql = generateSQL(query);
					log.debug("generateSQL 완료: {}", sql);

					log.debug("SQL 검증 시작");
					sqlValidationService.validate(sql);
					log.debug("SQL 검증 완료");

					String finalSql = sqlValidationService.enforceLimitClause(sql);
					log.debug("LIMIT 적용 완료: {}", finalSql);

					return finalSql;
				},
				// SQL 실행 로직
				(sql) -> {
					log.debug("SQL 실행 시작: {}", sql);
					List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
					log.debug("SQL 실행 완료: {} rows", result.size());
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
			log.debug("=== generateSQL 시작 ===");
			log.debug("userQuery: {}", userQuery);

			// 🔥 Step 1: FewShotExampleService null 체크
			log.debug("Step 1: FewShotExampleService 체크");
			if (fewShotExampleService == null) {
				log.error("FewShotExampleService가 null입니다!");
				throw new IllegalStateException("FewShotExampleService가 주입되지 않았습니다");
			}

			// 🔥 Step 2: 예제 찾기
			log.debug("Step 2: 관련 예제 검색");
			List<SqlExample> examples = fewShotExampleService.findRelevantExamples(userQuery, 3);
			log.debug("찾은 예제 수: {}", examples != null ? examples.size() : "NULL");

			// 🔥 Step 3: 예제 포맷팅
			log.debug("Step 3: 예제 포맷팅");
			String examplesPrompt = fewShotExampleService.formatExamplesForPrompt(examples);
			log.debug("포맷된 예제 길이: {}", examplesPrompt != null ? examplesPrompt.length() : "NULL");

			// 🔥 Step 4: 스키마 정보 가져오기
			log.debug("Step 4: 스키마 정보 가져오기");
			String schemaInfo = schemaService.getSchemaInfo();
			log.debug("스키마 정보 길이: {}", schemaInfo != null ? schemaInfo.length() : "NULL");

			String relationshipInfo = schemaService.getRelationshipInfo();
			log.debug("관계 정보 길이: {}", relationshipInfo != null ? relationshipInfo.length() : "NULL");

			// 🔥 Step 5: 프롬프트 생성
			log.debug("Step 5: 프롬프트 생성");
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

			log.debug("프롬프트 생성 완료 (길이: {})", promptTemplate.length());

			// 🔥 Step 6: LLM 호출
			log.debug("Step 6: LLM 호출 시작");
			String response = chatClient.prompt()
				.user(promptTemplate)
				.call()
				.content();

			log.debug("LLM 응답: {}", response);

			// 🔥 Step 7: 응답 정리
			log.debug("Step 7: 응답 정리");
			String cleanedSql = cleanSqlResponse(response);
			log.debug("정리된 SQL: {}", cleanedSql);

			log.debug("=== generateSQL 완료 ===");
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
