package com.ice.studyroom.domain.admin.presentation.dto.response;

import com.ice.studyroom.domain.admin.domain.entity.RoomTimeSlot;
import com.ice.studyroom.domain.admin.domain.type.DayOfWeekStatus;
import com.ice.studyroom.domain.schedule.domain.entity.Schedule;
import com.ice.studyroom.domain.reservation.domain.type.ScheduleSlotStatus;

import java.time.LocalTime;

public record AdminGetReservedResponse (
	Long id,
	String roomNumber,
	LocalTime startTime,
	LocalTime endTime,
	ScheduleSlotStatus status,
	DayOfWeekStatus dayOfWeek
){
	public static AdminGetReservedResponse from(RoomTimeSlot roomTimeSlot) {
		return new AdminGetReservedResponse(
			roomTimeSlot.getId(),
			roomTimeSlot.getRoomNumber(),
			roomTimeSlot.getStartTime(),
			roomTimeSlot.getEndTime(),
			roomTimeSlot.getStatus(),
			roomTimeSlot.getDayOfWeek()
		);
	}

	public static AdminGetReservedResponse from(Schedule schedule) {
		return new AdminGetReservedResponse(
			schedule.getId(),
			schedule.getRoomNumber(),
			schedule.getStartTime(),
			schedule.getEndTime(),
			schedule.getStatus(),
			schedule.getDayOfWeek()
		);
	}
}
