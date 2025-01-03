package com.ice.studyroom.domain.room.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ice.studyroom.domain.room.domain.entity.TimeSlot;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
}
