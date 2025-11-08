package com.ice.studyroom.domain.texttosql.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SqlRetryService {

	private static final int MAX_RETRY_ATTEMPTS = 3;

	/**
	 * SQL 실행을 재시도하는 메서드
	 *
	 * @param queryGenerator SQL 생성 함수
	 * @param sqlExecutor SQL 실행 함수
	 * @param userQuery 사용자 질문
	 * @return 쿼리 실행 결과
	 */
	public RetryResult executeWithRetry(
		SqlGenerator queryGenerator,
		SqlExecutor sqlExecutor,
		String userQuery
	) {
		String enhancedQuery = userQuery;
		String lastError = null;

		for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
			try {
				log.info("SQL 생성 시도 {}/{}: {}", attempt, MAX_RETRY_ATTEMPTS, enhancedQuery);

				// SQL 생성
				String sql = queryGenerator.generate(enhancedQuery);

				// SQL 실행
				List<Map<String, Object>> result = sqlExecutor.execute(sql);

				log.info("SQL 실행 성공 (시도 {}회)", attempt);
				return new RetryResult(true, sql, result, attempt, null);

			} catch (Exception e) {
				lastError = extractErrorMessage(e);
				log.warn("SQL 실행 실패 (시도 {}/{}): {}", attempt, MAX_RETRY_ATTEMPTS, lastError);

				if (attempt == MAX_RETRY_ATTEMPTS) {
					// 마지막 시도 실패
					log.error("모든 재시도 실패. 사용자에게 에러 반환");
					return new RetryResult(false, null, null, attempt, lastError);
				}

				// 에러 피드백으로 쿼리 개선
				enhancedQuery = enhanceQueryWithError(userQuery, lastError, attempt);
			}
		}

		// 이 코드는 실행되지 않지만, 컴파일을 위해 필요
		return new RetryResult(false, null, null, MAX_RETRY_ATTEMPTS, lastError);
	}

	/**
	 * Exception에서 안전하게 에러 메시지 추출
	 */
	private String extractErrorMessage(Exception e) {
		if (e.getMessage() != null && !e.getMessage().isEmpty()) {
			return e.getMessage();
		}

		// getMessage()가 null이면 예외 클래스 이름 사용
		return e.getClass().getSimpleName() + " (메시지 없음)";
	}

	/**
	 * 에러 메시지를 분석하여 쿼리를 개선합니다
	 */
	private String enhanceQueryWithError(String originalQuery, String error, int attempt) {
		StringBuilder enhanced = new StringBuilder(originalQuery);
		enhanced.append("\n\n[이전 시도 ").append(attempt).append("회 실패]");
		enhanced.append("\n에러: ").append(error);

		// 에러 패턴 분석 및 힌트 추가
		if (error != null) {
			// 에러 패턴 분석 및 힌트 추가
			if (error.contains("Unknown column")) {
				enhanced.append("\n힌트: 컬럼명을 다시 확인하세요. 스키마 정보를 참고하세요.");
			} else if (error.contains("Table") && error.contains("doesn't exist")) {
				enhanced.append("\n힌트: 테이블명을 다시 확인하세요. 정확한 테이블명을 사용하세요.");
			} else if (error.contains("syntax error")) {
				enhanced.append("\n힌트: SQL 문법을 다시 확인하세요.");
			} else if (error.contains("ambiguous")) {
				enhanced.append("\n힌트: 컬럼명이 모호합니다. 테이블 별칭을 사용하세요.");
			} else if (error.contains("허용되지 않는")) {
				enhanced.append("\n힌트: SELECT 문만 사용 가능합니다.");
			} else if (error.contains("접근이 제한된")) {
				enhanced.append("\n힌트: 민감한 컬럼에는 접근할 수 없습니다.");
			} else {
				enhanced.append("\n힌트: 쿼리를 다시 검토해주세요.");
			}
		}

		return enhanced.toString();
	}

	/**
	 * 재시도 결과를 담는 클래스
	 */
	public record RetryResult(
		boolean success,
		String sql,
		List<Map<String, Object>> data,
		int attempts,
		String error
	) {}

	/**
	 * SQL 생성 함수형 인터페이스
	 */
	@FunctionalInterface
	public interface SqlGenerator {
		String generate(String query) throws Exception;
	}

	/**
	 * SQL 실행 함수형 인터페이스
	 */
	@FunctionalInterface
	public interface SqlExecutor {
		List<Map<String, Object>> execute(String sql) throws Exception;
	}
}
