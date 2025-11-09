package com.ice.studyroom.domain.texttosql.application;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchemaService {

	private final JdbcTemplate jdbcTemplate;

	// 전체 스키마 캐시 (서버 시작 시 한 번만 로드)
	private Map<String, String> schemaCache;

	/**
	 * 서버 시작 시 스키마 캐시 로드
	 */
	@PostConstruct
	public void init() {
		loadSchemaCache();
	}

	/**
	 * 특정 테이블들의 스키마만 반환 (RAG)
	 */
	public String getSchemaInfo(Set<String> relevantTables) {
		if (schemaCache == null) {
			loadSchemaCache();
		}

		StringBuilder schema = new StringBuilder();
		schema.append("## 데이터베이스 스키마:\n\n");

		for (String tableName : relevantTables) {
			String tableSchema = schemaCache.get(tableName);
			if (tableSchema != null) {
				schema.append(tableSchema).append("\n");
			} else {
				log.warn("테이블 '{}' 스키마를 찾을 수 없음", tableName);
			}
		}

		log.debug("선택된 스키마 ({}개 테이블): {}", relevantTables.size(), relevantTables);
		return schema.toString();
	}

	/**
	 * 기존 메서드 (전체 스키마) - 하위 호환성 유지
	 */
	public String getSchemaInfo() {
		if (schemaCache == null) {
			loadSchemaCache();
		}

		StringBuilder schema = new StringBuilder();
		schema.append("## 데이터베이스 스키마:\n\n");

		for (String tableSchema : schemaCache.values()) {
			schema.append(tableSchema).append("\n");
		}

		return schema.toString();
	}

	/**
	 * 특정 테이블들의 관계 정보만 반환
	 */
	public String getRelationshipInfo(Set<String> relevantTables) {
		StringBuilder relationships = new StringBuilder();
		relationships.append("\n## 테이블 간 관계:\n\n");

		// reservation과 member 관계
		if (relevantTables.contains("reservation") && relevantTables.contains("member")) {
			relationships.append("""
                1. **reservation ↔ member**
                   - reservation.member_id → member.id
                   - 한 회원은 여러 예약을 가질 수 있음

                """);
		}

		// reservation 테이블 상세 정보
		if (relevantTables.contains("reservation")) {
			relationships.append("""
                2. **reservation 테이블 주요 컬럼:**
                   - first_schedule_id, second_schedule_id: schedule 참조
                   - schedule_date: 예약 날짜 (DATE)
                   - room_number: 방 번호 (VARCHAR)
                   - start_time, end_time: 예약 시간 (TIME)
                   - status: ENUM('RESERVED','ENTRANCE','LATE','NO_SHOW','CANCELLED','COMPLETED')
                   - enter_time, exit_time: 입실/퇴실 시간 (DATETIME)

                """);
		}

		// schedule과 room_time_slot 관계
		if (relevantTables.contains("schedule") && relevantTables.contains("room_time_slot")) {
			relationships.append("""
            3. **schedule ↔ room_time_slot**
               - schedule.room_time_slot_id → room_time_slot.id
               - schedule에는 특정 날짜의 예약 가능 정보 포함
               - room_time_slot에는 방 정보(room_number, capacity)와 요일별 시간대 정보 포함

            """);
		}

		// penalty와 member, reservation 관계
		if (relevantTables.contains("penalty")) {
			relationships.append("""
            4. **penalty ↔ member, reservation**
               - penalty.member_id → member.id
               - penalty.reservation_id → reservation.id
               - 패널티 사유: CANCEL, LATE, NO_SHOW, ADMIN

            """);
		}

		return relationships.toString();
	}

	/**
	 * 기존 메서드 (전체 관계) - 하위 호환성 유지
	 */
	public String getRelationshipInfo() {
		return """
            ## 테이블 간 관계:

            1. **reservation ↔ member**
               - reservation.member_id → member.id
               - 한 회원은 여러 예약을 가질 수 있음

            2. **reservation 테이블 주요 컬럼:**
               - first_schedule_id, second_schedule_id: schedule 참조
               - schedule_date: 예약 날짜 (DATE)
               - room_number: 방 번호 (VARCHAR)
               - start_time, end_time: 예약 시간 (TIME)
               - status: ENUM('RESERVED','ENTRANCE','LATE','NO_SHOW','CANCELLED','COMPLETED')

            3. **중요: reservation은 schedule 정보를 자체적으로 보유**
               - schedule 테이블과 JOIN 불필요한 경우가 많음
               - 날짜/시간은 reservation 테이블에서 직접 조회

            """;
	}

	/**
	 * 스키마 캐시 로드 (서버 시작 시 한 번)
	 */
	private void loadSchemaCache() {
		log.info("스키마 정보 캐싱 시작...");
		schemaCache = new LinkedHashMap<>();

		try {
			String sql = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = DATABASE()";
			List<String> tables = jdbcTemplate.queryForList(sql, String.class);

			for (String tableName : tables) {
				String tableSchema = loadTableSchema(tableName);
				schemaCache.put(tableName, tableSchema);
			}

			log.info("스키마 정보 캐싱 완료: {}개 테이블", schemaCache.size());

		} catch (Exception e) {
			log.error("스키마 캐싱 실패", e);
			schemaCache = new LinkedHashMap<>(); // 빈 캐시로 초기화
		}
	}

	/**
	 * 특정 테이블의 스키마 정보 로드
	 */
	private String loadTableSchema(String tableName) {
		StringBuilder tableInfo = new StringBuilder();
		tableInfo.append(String.format("### %s 테이블:\n", tableName));

		try {
			String sql = """
                SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_KEY, COLUMN_DEFAULT, EXTRA
                FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?
                ORDER BY ORDINAL_POSITION
                """;

			List<Map<String, Object>> columns = jdbcTemplate.queryForList(sql, tableName);

			for (Map<String, Object> column : columns) {
				String columnName = (String) column.get("COLUMN_NAME");
				String columnType = (String) column.get("COLUMN_TYPE");
				String nullable = (String) column.get("IS_NULLABLE");
				String key = (String) column.get("COLUMN_KEY");
				String extra = (String) column.get("EXTRA");

				tableInfo.append(String.format("  - %s: %s", columnName, columnType));

				if ("NO".equals(nullable)) {
					tableInfo.append(" (NOT NULL)");
				}
				if ("PRI".equals(key)) {
					tableInfo.append(" (PRIMARY KEY)");
				}
				if ("MUL".equals(key)) {
					tableInfo.append(" (FOREIGN KEY)");
				}
				if (extra != null && !extra.isEmpty()) {
					tableInfo.append(String.format(" (%s)", extra));
				}

				tableInfo.append("\n");
			}

		} catch (Exception e) {
			log.error("테이블 '{}' 스키마 로드 실패", tableName, e);
			tableInfo.append("  - (스키마 정보 로드 실패)\n");
		}

		return tableInfo.toString();
	}
}
