package com.ice.studyroom.domain.admin.infrastructure.persistence;

import com.ice.studyroom.domain.admin.domain.entity.RoomTimeSlot;
import com.ice.studyroom.domain.admin.domain.type.DayOfWeekStatus;
import com.ice.studyroom.domain.admin.domain.type.RoomType;
import com.ice.studyroom.domain.admin.presentation.dto.response.RoomInfoResponse;
import com.ice.studyroom.domain.reservation.domain.type.ScheduleSlotStatus;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RoomTimeSlotRepository extends JpaRepository<RoomTimeSlot, Long> {

	List<RoomTimeSlot> findByStatus(ScheduleSlotStatus status);
	List<RoomTimeSlot> findByDayOfWeek(DayOfWeekStatus dayOfWeekStatus);

	@Query("""
    SELECT DISTINCT new com.ice.studyroom.domain.admin.presentation.dto.response.RoomInfoResponse(r.roomNumber, r.roomType, r.capacity)
    FROM RoomTimeSlot r
""")
	List<RoomInfoResponse> findDistinctRoomInfo();
 }
