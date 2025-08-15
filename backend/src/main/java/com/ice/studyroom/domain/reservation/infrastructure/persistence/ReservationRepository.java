package com.ice.studyroom.domain.reservation.infrastructure.persistence;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ice.studyroom.domain.membership.domain.entity.Member;
import com.ice.studyroom.domain.membership.domain.vo.Email;
import com.ice.studyroom.domain.reservation.domain.entity.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

	List<Reservation> findByMember(Member member);

	List<Reservation> findByScheduleDateAndEndTime(LocalDate scheduleDate, LocalTime time);

	List<Reservation> findByRoomNumberAndScheduleDateAndStartTime(String roomNumber, LocalDate scheduleDate, LocalTime startTime);

	List<Reservation> findByFirstScheduleId(Long firstScheduleId);

	Optional<Reservation> findByQrToken(String qrToken);

	@Query("SELECT r FROM Reservation r WHERE r.member.email = :email ORDER BY r.createdAt DESC LIMIT 1")
	Optional<Reservation> findLatestReservationByMemberEmail(@Param("email") Email email);
}
