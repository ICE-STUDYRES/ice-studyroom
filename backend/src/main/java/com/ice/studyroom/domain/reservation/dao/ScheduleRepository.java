package com.ice.studyroom.domain.reservation.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ice.studyroom.domain.reservation.domain.entity.Schedule;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
}
