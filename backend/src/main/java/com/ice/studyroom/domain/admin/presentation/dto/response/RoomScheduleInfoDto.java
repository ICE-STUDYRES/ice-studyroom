package com.ice.studyroom.domain.admin.presentation.dto.response;

import com.ice.studyroom.domain.admin.domain.entity.RoomTimeSlot;
import com.ice.studyroom.domain.admin.domain.type.DayOfWeekStatus;
import com.ice.studyroom.domain.reservation.domain.entity.Schedule;
import com.ice.studyroom.domain.reservation.domain.type.ScheduleSlotStatus;

import java.time.LocalTime;

public record RoomScheduleInfoDto(
	Long id,
	String roomNumber,
	ScheduleSlotStatus status,
	LocalTime startTime,
	LocalTime endTime,
	DayOfWeekStatus dayOfWeekStatus

) {
	public static RoomScheduleInfoDto from(RoomTimeSlot roomTimeSlot) {
		return new RoomScheduleInfoDto(
			roomTimeSlot.getId(),
			roomTimeSlot.getRoomNumber(),
			roomTimeSlot.getStatus(),
			roomTimeSlot.getStartTime(),
			roomTimeSlot.getEndTime(),
			roomTimeSlot.getDayOfWeek()
		);
	}

	public static RoomScheduleInfoDto from(Schedule schedule){
		return new RoomScheduleInfoDto(
			schedule.getId(),
			schedule.getRoomNumber(),
			schedule.getStatus(),
			schedule.getStartTime(),
			schedule.getEndTime(),
			schedule.getDayOfWeek()
		);
	}
}
