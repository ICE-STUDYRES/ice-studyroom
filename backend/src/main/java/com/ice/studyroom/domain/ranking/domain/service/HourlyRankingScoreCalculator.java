package com.ice.studyroom.domain.ranking.domain.service;

import com.ice.studyroom.domain.reservation.domain.entity.Reservation;
import com.ice.studyroom.domain.reservation.domain.type.ReservationStatus;
import com.ice.studyroom.domain.schedule.domain.entity.Schedule;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class HourlyRankingScoreCalculator implements RankingScoreCalculator {

	private static final int SCORE_PER_HOUR = 10;

	@Override
	public int calculate(Reservation reservation, ReservationStatus status) {

		// 입실 성공한 경우만 점수 지급 (지각 포함)
		if (status != ReservationStatus.ENTRANCE && status != ReservationStatus.LATE) {
			return 0;
		}

		LocalDateTime start = LocalDateTime.of(
			reservation.getScheduleDate(),
			reservation.getStartTime()
		);

		LocalDateTime end = LocalDateTime.of(
			reservation.getScheduleDate(),
			reservation.getEndTime()
		);

		long hours = Duration.between(start, end).toHours();

		return (int) hours * SCORE_PER_HOUR;
	}

	@Override
	public int calculateForSchedule(Schedule schedule) {

		LocalDateTime start = LocalDateTime.of(
				schedule.getScheduleDate(),
				schedule.getStartTime()
		);

		LocalDateTime end = LocalDateTime.of(
				schedule.getScheduleDate(),
				schedule.getEndTime()
		);

		long hours = Duration.between(start, end).toHours();

		return (int) hours * SCORE_PER_HOUR;
	}

}
