package com.ice.studyroom.domain.admin.presentation.dto.response;

import com.ice.studyroom.domain.admin.domain.entity.RoomTimeSlot;
import com.ice.studyroom.domain.admin.domain.type.DayOfWeekStatus;

import java.time.LocalTime;

public record RoomScheduleInfoDto(
	Long roomTimeSlotId,
	String roomNumber,
	LocalTime startTime,
	LocalTime endTime,
	DayOfWeekStatus dayOfWeekStatus

) {
	public static RoomScheduleInfoDto from(RoomTimeSlot roomTimeSlot) {
		return new RoomScheduleInfoDto(
			roomTimeSlot.getId(),
			roomTimeSlot.getRoomNumber(),
			roomTimeSlot.getStartTime(),
			roomTimeSlot.getEndTime(),
			roomTimeSlot.getDayOfWeek()
		);
	}
}
