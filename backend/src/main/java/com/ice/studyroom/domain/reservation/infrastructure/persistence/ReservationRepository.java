package com.ice.studyroom.domain.reservation.infrastructure.persistence;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ice.studyroom.domain.reservation.domain.entity.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
	List<Reservation> findByScheduleDate(LocalDate date);

	List<Reservation> findByUserEmail(String email);

	Optional<Reservation> findFirstByUserEmailOrderByCreatedAtDesc(String email);

	//todo: 테스트 코드에 사용되는 거 수정
	List<Reservation> findByEndTimeBetween(LocalTime time1, LocalTime time2);

	List<Reservation> findByScheduleDateAndEndTimeBetween(LocalDate scheduleDate, LocalTime time1, LocalTime time2);

	List<Reservation> findByRoomNumberAndScheduleDateAndStartTime(String roomNumber, LocalDate scheduleDate, LocalTime startTime);
}
