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
           ## 테이블 간 관계

           1. **reservation ↔ member**
              - reservation.member_id → member.id
              - 한 회원은 여러 예약을 가질 수 있음

           2. **reservation ↔ schedule**
              - reservation.first_schedule_id → schedule.id (메인 스케줄)
              - reservation.second_schedule_id → schedule.id (선택적, 2시간 예약 시 사용)

           3. **reservation 자체 보유 정보**
              - schedule_date: 예약 날짜 (DATE)
              - room_number: 방 번호 (VARCHAR)
              - start_time, end_time: 예약 시간 (TIME)
              대부분의 쿼리에서 schedule 테이블 JOIN 불필요!

           ## 중요 비즈니스 규칙

           1. **그룹 예약**
              - 같은 first_schedule_id를 가진 예약들은 하나의 그룹
              - is_holder = 1인 예약이 그룹의 대표자

           2. **예약 상태 (status ENUM)**
              - RESERVED: 예약됨 (기본값)
              - ENTRANCE: 입실함
              - LATE: 지각 (입실 시간 초과)
              - NO_SHOW: 노쇼 (입실 안 함)
              - CANCELLED: 취소됨
              - COMPLETED: 완료됨

           3. **입실/퇴실 시간**
              - enter_time: 실제 입실 시간 (NULL 가능)
              - exit_time: 실제 퇴실 시간 (NULL 가능)
              - status = 'ENTRANCE'인 예약만 실제 사용 중

           4. **날짜/시간 쿼리 예시**
              - 오늘 예약: WHERE schedule_date = CURDATE()
              - 어제 예약: WHERE schedule_date = DATE_SUB(CURDATE(), INTERVAL 1 DAY)
              - 특정 시간대: WHERE start_time >= '14:00:00' AND end_time <= '16:00:00'

           """;
	}
}
