package com.ice.studyroom.domain.texttosql.application;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.ice.studyroom.global.exception.BusinessException;
import com.ice.studyroom.global.type.StatusCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class SqlValidationService {

	private static final List<String> DANGEROUS_KEYWORDS = List.of(
		"DROP", "DELETE", "UPDATE", "INSERT", "ALTER",
		"CREATE", "TRUNCATE", "EXEC", "EXECUTE", "GRANT",
		"REVOKE", "COMMIT", "ROLLBACK"
	);

	private static final List<String> SENSITIVE_COLUMNS = List.of(
		"password", "pwd", "passwd", "secret", "api_key",
		"token", "private_key"
	);

	private static final int MAX_RESULT_ROWS = 1000;

	/**
	 * SQL 보안 검증을 수행합니다.
	 * 3단계 검증: 키워드 체크 → AST 파싱 → 민감 컬럼 체크
	 */
	public void validate(String sql) {
		if (sql == null || sql.trim().isEmpty()) {
			throw new BusinessException(StatusCode.BAD_REQUEST, "SQL이 비어있습니다.");
		}

		log.debug("SQL 검증 시작: {}", sql);

		// Layer 1: 위험 키워드 체크
		checkDangerousKeywords(sql);

		// Layer 2: AST 파싱으로 구조 검증
		validateSqlStructure(sql);

		// Layer 3: 민감 컬럼 접근 차단
		checkSensitiveColumns(sql);

		log.debug("SQL 검증 통과");
	}

	/**
	 * SQL에 LIMIT 절을 강제로 추가합니다.
	 */
	public String enforceLimitClause(String sql) {
		String upperSql = sql.toUpperCase();

		if (!upperSql.contains("LIMIT")) {
			log.debug("LIMIT 절 추가: {}", MAX_RESULT_ROWS);
			return sql.trim() + " LIMIT " + MAX_RESULT_ROWS;
		}

		return sql;
	}

	private void checkDangerousKeywords(String sql) {
		String upperSql = sql.toUpperCase();

		for (String keyword : DANGEROUS_KEYWORDS) {
			if (upperSql.contains(keyword)) {
				log.warn("위험한 SQL 키워드 감지: {}", keyword);
				throw new BusinessException(
					StatusCode.BAD_REQUEST,
					"허용되지 않는 SQL 명령어입니다: " + keyword
				);
			}
		}
	}

	private void validateSqlStructure(String sql) {
		try {
			SQLStatement stmt = SQLUtils.parseSingleMysqlStatement(sql);

			if (!(stmt instanceof SQLSelectStatement)) {
				log.warn("SELECT 이외의 SQL 구문 시도: {}", stmt.getClass().getSimpleName());
				throw new BusinessException(
					StatusCode.BAD_REQUEST,
					"SELECT 문만 실행 가능합니다."
				);
			}

		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			log.error("SQL 파싱 실패: {}", sql, e);
			throw new BusinessException(
				StatusCode.BAD_REQUEST,
				"잘못된 SQL 구문입니다: " + e.getMessage()
			);
		}
	}

	private void checkSensitiveColumns(String sql) {
		String lowerSql = sql.toLowerCase();

		for (String column : SENSITIVE_COLUMNS) {
			if (lowerSql.contains(column)) {
				log.warn("민감 컬럼 접근 시도: {}", column);
				throw new BusinessException(
					StatusCode.FORBIDDEN,
					"접근이 제한된 컬럼입니다: " + column
				);
			}
		}
	}
}
