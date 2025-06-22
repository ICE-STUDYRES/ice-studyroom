package com.ice.studyroom.domain.reservation.infrastructure.persistence;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import com.ice.studyroom.domain.reservation.domain.entity.Schedule;
import com.ice.studyroom.domain.reservation.domain.type.ScheduleSlotStatus;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

	List<Schedule> findByScheduleDate(LocalDate date);

	List<Schedule> findAllByIdIn(List<Long> ids);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "500")})
	@Query("SELECT s FROM Schedule s WHERE s.id IN :ids ORDER BY s.id")
	List<Schedule> findByIdsWithPessimisticLock(@Param("ids") List<Long> ids);

	List<Schedule> findByScheduleDateAndStatus(LocalDate date, ScheduleSlotStatus status);

	List<Schedule> findByScheduleDateAndRoomTimeSlotIdIn(LocalDate date, List<Long> roomTimeSlotId);

}
