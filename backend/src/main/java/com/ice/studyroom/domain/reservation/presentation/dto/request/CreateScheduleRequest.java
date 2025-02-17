package com.ice.studyroom.domain.reservation.presentation.dto.request;

import java.time.LocalDate;
import java.time.LocalTime;

import com.ice.studyroom.domain.admin.domain.type.DayOfWeekStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CreateScheduleRequest {
	private LocalDate scheduleDate;
	private String roomNumber;
	private LocalTime startTime;
	private LocalTime endTime;
	private Integer capacity;
	private DayOfWeekStatus dayOfWeek;
}
