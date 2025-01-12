package com.ice.studyroom.domain.room.dao;

import com.ice.studyroom.domain.room.domain.entity.RoomTimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomTimeSlotRepository extends JpaRepository<RoomTimeSlot, Long> {
}
