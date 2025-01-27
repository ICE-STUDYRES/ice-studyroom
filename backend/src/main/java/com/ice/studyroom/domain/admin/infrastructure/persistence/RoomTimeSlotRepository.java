package com.ice.studyroom.domain.admin.infrastructure.persistence;

import com.ice.studyroom.domain.admin.domain.entity.RoomTimeSlot;
import com.ice.studyroom.domain.admin.domain.type.RoomTimeSlotStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomTimeSlotRepository extends JpaRepository<RoomTimeSlot, Long> {
    List<RoomTimeSlot> findByStatus(RoomTimeSlotStatus status);
}
