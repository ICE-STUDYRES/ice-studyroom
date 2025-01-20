package com.ice.studyroom.domain.room_timeslot.infrastructure.persistence;

import com.ice.studyroom.domain.room_timeslot.domain.entity.RoomTimeSlot;
import com.ice.studyroom.domain.room_timeslot.domain.type.DayOfWeekStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.Optional;

public interface RoomTimeSlotRepository extends JpaRepository<RoomTimeSlot, Long> {

}
