package com.ice.studyroom.domain.reservation.infrastructure.persistence;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ice.studyroom.domain.reservation.domain.entity.Schedule;
import com.ice.studyroom.domain.reservation.domain.type.ScheduleSlotStatus;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

	List<Schedule> findByScheduleDate(LocalDate date);

	List<Schedule> findAllByIdIn(List<Long> ids);

	List<Schedule> findByScheduleDateAndStatus(LocalDate date, ScheduleSlotStatus status);

	List<Schedule> findByScheduleDateAndRoomTimeSlotIdIn(LocalDate date, List<Long> roomTimeSlotId);

}
