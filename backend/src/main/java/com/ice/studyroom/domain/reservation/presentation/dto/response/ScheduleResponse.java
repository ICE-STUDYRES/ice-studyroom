package com.ice.studyroom.domain.reservation.presentation.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.ice.studyroom.domain.reservation.domain.type.ScheduleStatus;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScheduleResponse {
	private Long id;
	private LocalDate scheduleDate;
	private String roomNumber;
	private LocalTime startTime;
	private LocalTime endTime;
	private Integer capacity;
	private ScheduleStatus status;
	private Long reservedById;
	private String reservedByName;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
