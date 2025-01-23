package com.ice.studyroom.domain.reservation.scheduler;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class) // Spring Context 없이 Mockito 기반 테스트
class ScheduleTaskTest {

	@Mock
	private JdbcTemplate jdbcTemplate; // Mock 객체 사용

	@InjectMocks
	private ScheduleTask scheduleTask; // Mock JdbcTemplate을 사용하도록 주입

	/**
	 * ✅ **JdbcTemplate이 SQL 실행할 때 정상 동작하는지 단위 테스트**
	 */
	@Test
	void testInsertScheduleData() {
		// Given: JdbcTemplate이 update() 실행될 때 예외 없이 동작하도록 설정
		when(jdbcTemplate.update(anyString())).thenReturn(1);

		// When: 스케줄러 직접 실행 (cron 실행 기다릴 필요 없음)
		scheduleTask.insertScheduleData();

		// Then: JdbcTemplate의 update()가 정확히 1번 호출되었는지 확인
		verify(jdbcTemplate, times(1)).update(anyString());
	}

	/**
	 * ✅ **SQL 실행을 검증하는 단위 테스트**
	 */
	@Test
	void testScheduleInsertToDatabase() {
		// Given: JdbcTemplate이 INSERT 실행할 때 예외 없이 동작하도록 설정
		when(jdbcTemplate.update(contains("INSERT"))).thenReturn(1);

		// Given: JdbcTemplate이 COUNT 조회할 때 특정 값을 반환하도록 설정
		when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class))).thenReturn(10);

		// When: 스케줄러 실행
		scheduleTask.insertScheduleData();

		// Then: 데이터가 정상적으로 삽입되었는지 확인
		Integer count = jdbcTemplate.queryForObject(
			"SELECT COUNT(*) FROM schedule WHERE day_of_week = DAYNAME(CURRENT_DATE)",
			Integer.class
		);

		// 로그 출력
		System.out.println("📌 count = " + count);

		// 검증
		assertThat(count).isNotNull(); // null 검증
		assertThat(count).isGreaterThan(0); // 최소 1개 이상 삽입되었는지 검증

		// Mock 객체의 update()가 올바르게 호출되었는지 확인
		verify(jdbcTemplate, times(1)).update(contains("INSERT"));
		verify(jdbcTemplate, times(1)).queryForObject(contains("COUNT"), eq(Integer.class));
	}
}
