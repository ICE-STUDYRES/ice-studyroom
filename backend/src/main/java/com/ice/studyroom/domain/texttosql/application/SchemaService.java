package com.ice.studyroom.domain.texttosql.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SchemaService {

	@Qualifier("readOnlyJdbcTemplate")
	private final JdbcTemplate jdbcTemplate;

	public SchemaService(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * DB 스키마 정보를 문자열로 반환합니다.
	 * LLM 프롬프트에 포함될 스키마 정보입니다.
	 */
	public String getSchemaInfo() {
		StringBuilder schema = new StringBuilder();

		schema.append("## 데이터베이스 스키마 정보\n\n");

		// 주요 테이블 정보
		schema.append(getReservationTableSchema());
		schema.append(getScheduleTableSchema());
		schema.append(getRoomTableSchema());
		schema.append(getMemberTableSchema());

		return schema.toString();
	}

	private String getReservationTableSchema() {
		return """
            ### 1. reservation (예약 테이블)
            - id: BIGINT (PK) - 예약 ID
            - member_id: BIGINT (FK) - 회원 ID
            - schedule_id: BIGINT (FK) - 스케줄 ID
            - first_schedule_id: BIGINT - 첫 스케줄 ID (그룹 예약용)
            - status: VARCHAR - 예약 상태 (PENDING, CONFIRMED, COMPLETED, CANCELLED)
            - is_entered: BOOLEAN - 입실 여부
            - created_at: DATETIME - 생성일시
            - updated_at: DATETIME - 수정일시

            """;
	}

	private String getScheduleTableSchema() {
		return """
            ### 2. schedule (스케줄 테이블)
            - id: BIGINT (PK) - 스케줄 ID
            - room_time_slot_id: BIGINT (FK) - 룸 타임슬롯 ID
            - schedule_date: DATE - 스케줄 날짜
            - start_time: TIME - 시작 시간
            - end_time: TIME - 종료 시간
            - status: VARCHAR - 상태 (AVAILABLE, RESERVED, UNAVAILABLE)
            - current_res: INT - 현재 예약 수
            - capacity: INT - 수용 인원

            """;
	}

	private String getRoomTableSchema() {
		return """
            ### 3. room_time_slot (룸 타임슬롯 테이블)
            - id: BIGINT (PK)
            - room_number: VARCHAR - 방 번호 (예: 201, 202)
            - room_type: VARCHAR - 방 타입 (INDIVIDUAL, GROUP)
            - start_time: TIME - 시작 시간
            - end_time: TIME - 종료 시간
            - capacity: INT - 수용 인원
            - min_res: INT - 최소 예약 인원
            - day_of_week: VARCHAR - 요일

            """;
	}

	private String getMemberTableSchema() {
		return """
            ### 4. member (회원 테이블) - 민감정보 제외
            - id: BIGINT (PK) - 회원 ID
            - email: VARCHAR - 이메일
            - name: VARCHAR - 이름
            - is_penalty: BOOLEAN - 패널티 여부
            - created_at: DATETIME - 가입일시
            ⚠️ password 컬럼은 조회 불가능

            """;
	}

	/**
	 * 테이블 간 관계 정보를 반환합니다.
	 */
	public String getRelationshipInfo() {
		return """
            ## 테이블 관계
            - reservation.member_id → member.id
            - reservation.schedule_id → schedule.id
            - schedule.room_time_slot_id → room_time_slot.id

            ## 중요 비즈니스 규칙
            - 그룹 예약: first_schedule_id가 같은 reservation들은 하나의 그룹
            - 입실 확인: is_entered = true인 예약만 실제 사용
            - 예약 상태: COMPLETED는 완료된 예약, CANCELLED는 취소된 예약
            """;
	}
}
