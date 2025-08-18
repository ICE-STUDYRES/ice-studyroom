package com.ice.studyroom.domain.reservation.infrastructure.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class VacancyNotificationRequest {
	private String email;
	private String roomName;
	private String eventDate;
	private Long scheduleId;
	private long timestamp;
	private String startTime;
	private String endTime;
}
