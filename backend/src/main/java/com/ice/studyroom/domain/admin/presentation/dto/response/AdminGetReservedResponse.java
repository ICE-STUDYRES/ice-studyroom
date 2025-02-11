package com.ice.studyroom.domain.admin.presentation.dto.response;

import com.ice.studyroom.domain.admin.domain.entity.RoomTimeSlot;
import com.ice.studyroom.domain.admin.domain.type.DayOfWeekStatus;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record AdminGetReservedResponse (
	String roomNumber,
	LocalTime startTime,
	DayOfWeekStatus dayOfWeek
){
	public static AdminGetReservedResponse from(RoomTimeSlot roomTimeSlot) {
		return new AdminGetReservedResponse(
			roomTimeSlot.getRoomNumber(),
			roomTimeSlot.getStartTime(),
			roomTimeSlot.getDayOfWeek()
		);
	}
}
