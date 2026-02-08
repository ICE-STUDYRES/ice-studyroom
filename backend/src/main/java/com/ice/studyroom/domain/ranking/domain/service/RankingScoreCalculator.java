package com.ice.studyroom.domain.ranking.domain.service;

import com.ice.studyroom.domain.reservation.domain.entity.Reservation;
import com.ice.studyroom.domain.reservation.domain.type.ReservationStatus;

public interface RankingScoreCalculator {

	int calculate(Reservation reservation, ReservationStatus status);
}
