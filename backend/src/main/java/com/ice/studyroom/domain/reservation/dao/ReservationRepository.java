package com.ice.studyroom.domain.reservation.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ice.studyroom.domain.reservation.domain.entity.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
}
