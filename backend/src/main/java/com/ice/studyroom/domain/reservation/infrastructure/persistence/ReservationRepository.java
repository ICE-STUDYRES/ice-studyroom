package com.ice.studyroom.domain.reservation.infrastructure.persistence;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ice.studyroom.domain.reservation.domain.entity.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

	List<Reservation> findByUserEmail(String email);

	List<Reservation> findByScheduleDateAndEndTime(LocalDate scheduleDate, LocalTime time);

	List<Reservation> findByRoomNumberAndScheduleDateAndStartTime(String roomNumber, LocalDate scheduleDate, LocalTime startTime);

	List<Reservation> findByFirstScheduleId(Long firstScheduleId);

	@Query("SELECT r FROM Reservation r WHERE r.userEmail = :email ORDER BY r.createdAt DESC LIMIT 1")
	Optional<Reservation> findLatestReservationByUserEmail(@Param("email") String email);
}
