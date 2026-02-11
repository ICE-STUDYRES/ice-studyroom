package com.ice.studyroom.domain.notification.presentation.dto.response;

import com.ice.studyroom.domain.notification.domain.entity.Notification;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationResponse {

	private String eventType;

	private int rank;
	private Integer previousRank;
	private Integer gapWithUpper;

	private boolean isRead;
	private LocalDateTime createdAt;

	public static NotificationResponse from(Notification notification) {
		return NotificationResponse.builder()
			.eventType(notification.getEventType().name())
			.rank(notification.getRank())
			.previousRank(notification.getPreviousRank())
			.gapWithUpper(notification.getGapWithUpper())
			.isRead(notification.isRead())
			.createdAt(notification.getCreatedAt())
			.build();
	}
}
