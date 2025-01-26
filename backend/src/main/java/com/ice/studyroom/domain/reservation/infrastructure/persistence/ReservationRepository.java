package com.ice.studyroom.domain.reservation.infrastructure.persistence;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ice.studyroom.domain.reservation.domain.entity.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
	List<Reservation> findByUserEmail(String email);
	// 시작 시간에 해당하는 예약 조회
	@Query("SELECT r FROM Reservation r " +
		"WHERE r.scheduleDate = :date " +
		"AND r.startTime = :time")
	List<Reservation> findByScheduleDateAndStartTime(
		@Param("date") LocalDate date,
		@Param("time") LocalTime time
	);

	// 종료 시간에 해당하는 예약 조회
	@Query("SELECT r FROM Reservation r " +
		"WHERE r.scheduleDate = :date " +
		"AND r.endTime = :time")
	List<Reservation> findByScheduleDateAndEndTime(
		@Param("date") LocalDate date,
		@Param("time") LocalTime time
	);

	// 특정 날짜에 해당하는 예약 조회
	@Query("SELECT r FROM Reservation r WHERE r.scheduleDate = :date")
	List<Reservation> findByScheduleDate(@Param("date") LocalDate date);
}
