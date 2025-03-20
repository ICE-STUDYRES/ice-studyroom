package com.ice.studyroom.domain.reservation.infrastructure.persistence;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ice.studyroom.domain.reservation.domain.entity.Schedule;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

	List<Schedule> findByScheduleDate(LocalDate date);
}
