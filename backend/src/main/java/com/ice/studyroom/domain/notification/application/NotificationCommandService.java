package com.ice.studyroom.domain.notification.application;

import com.ice.studyroom.domain.notification.domain.entity.Notification;
import com.ice.studyroom.domain.notification.infrastructure.NotificationRepository;
import com.ice.studyroom.domain.notification.type.NotificationEventType;
import com.ice.studyroom.domain.ranking.application.event.dto.RankingUserChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationCommandService {

	private final NotificationRepository notificationRepository;

	@Transactional
	public void saveFromRankingEvent(RankingUserChangedEvent event) {

		// 중복 방지
		if (notificationRepository.existsByEventId(event.eventId())) {
			return;
		}

		NotificationEventType type =
			NotificationEventType.valueOf(event.eventType().name());

		Notification notification = Notification.create(
			event.memberId(),
			type,
			event.currentRank(),
			event.previousRank(),
			event.score(),
			event.gapWithUpper(),
			event.eventId()
		);

		notificationRepository.save(notification);
	}
}
