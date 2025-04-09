package com.ice.studyroom.domain.reservation.scheduler;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class ScheduleTask {

	private final JdbcTemplate jdbcTemplate;
	private final Clock clock;

	@Transactional
	@Scheduled(cron = "${schedule.insert.cron}")
	public void insertScheduleData() {
		LocalDateTime now = LocalDateTime.now(clock);
		LocalDate todayDate = now.toLocalDate();
		LocalTime todayTime = now.toLocalTime();

		log.info("Processing create Today Schedule date: {} and time: {}", todayDate, todayTime);

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
