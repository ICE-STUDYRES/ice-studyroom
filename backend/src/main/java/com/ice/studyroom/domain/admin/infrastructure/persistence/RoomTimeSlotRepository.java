package com.ice.studyroom.domain.admin.infrastructure.persistence;

import com.ice.studyroom.domain.admin.domain.entity.RoomTimeSlot;
import com.ice.studyroom.domain.admin.domain.type.DayOfWeekStatus;
import com.ice.studyroom.domain.admin.domain.type.RoomTimeSlotStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface RoomTimeSlotRepository extends JpaRepository<RoomTimeSlot, Long> {
	List<RoomTimeSlot> findByStatus(RoomTimeSlotStatus status);
	Optional<RoomTimeSlot> findByRoomNumberAndStartTimeAndDayOfWeek(
		String roomNumber,
		LocalTime startTime,
		DayOfWeekStatus dayOfWeek
	);
}
