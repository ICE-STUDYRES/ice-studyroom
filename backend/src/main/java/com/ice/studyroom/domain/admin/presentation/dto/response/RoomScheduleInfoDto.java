package com.ice.studyroom.domain.admin.presentation.dto.response;

import com.ice.studyroom.domain.admin.domain.entity.RoomTimeSlot;
import com.ice.studyroom.domain.admin.domain.type.DayOfWeekStatus;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record AdminRoomResponse(
	Long roomTimeSlotId,
	String roomNumber,
	LocalTime startTime,
	LocalTime endTime,
	DayOfWeekStatus dayOfWeekStatus

) {
	public static AdminRoomResponse from(RoomTimeSlot roomTimeSlot) {
		return new AdminRoomResponse(
			roomTimeSlot.getId(),
			roomTimeSlot.getRoomNumber(),
			roomTimeSlot.getStartTime(),
			roomTimeSlot.getEndTime(),
			roomTimeSlot.getDayOfWeek()
		);
	}
}
