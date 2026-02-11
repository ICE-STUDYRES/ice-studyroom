package com.ice.studyroom.domain.ranking.domain.service;

import com.ice.studyroom.domain.reservation.domain.entity.Reservation;
import com.ice.studyroom.domain.reservation.domain.type.ReservationStatus;
import com.ice.studyroom.domain.schedule.domain.entity.Schedule;

public interface RankingScoreCalculator {

	int calculate(Reservation reservation, ReservationStatus status);

	// 연장 시 추가된 스케줄 기반 점수 계산
	int calculateForSchedule(Schedule schedule);
}
