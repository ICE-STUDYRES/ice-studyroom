package com.ice.studyroom.domain.reservation.scheduler;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@EnableScheduling
public class ScheduleTask {

	private final JdbcTemplate jdbcTemplate;

	public ScheduleTask(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Transactional
	@Scheduled(cron = "${schedule.insert.cron}")
	public void insertScheduleData() {
		String sql = """
				INSERT INTO schedule (
					schedule_date,
					room_number,
					room_time_slot_id,
					room_type,
					start_time,
					end_time,
					capacity,
					min_res,
					status,
					day_of_week,
					created_at,
					updated_at
				)
				SELECT\s
					CONVERT_TZ(CURRENT_DATE, 'UTC', 'Asia/Seoul'),
					r.room_number,
					r.id AS room_time_slot_id,
					r.room_type,
					r.start_time,
					r.end_time,
					r.capacity,
					r.min_res,
					r.status,
					DAYNAME(CONVERT_TZ(CURRENT_DATE, 'UTC', 'Asia/Seoul')),
					NOW(),
					NOW()
				FROM room_time_slot r
				WHERE r.day_of_week = DAYNAME(CONVERT_TZ(CURRENT_DATE, 'UTC', 'Asia/Seoul'));
			""";
		jdbcTemplate.update(sql);
		System.out.println("Schedule inserted successfully at " + java.time.LocalDateTime.now());
	}
}
