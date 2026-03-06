package com.ice.studyroom.domain.notification.infrastructure.kafka;

import com.ice.studyroom.domain.notification.application.NotificationCommandService;
import com.ice.studyroom.domain.ranking.application.event.dto.RankingUserChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class RankingNotificationConsumer {

	private final NotificationCommandService notificationCommandService;

	@KafkaListener(
		topics = "RANKING_USER_CHANGED_EVENT",
		groupId = "ranking-notification-group"
	)
	public void handle(RankingUserChangedEvent event) {

		log.info("[NOTIFICATION] 랭킹 이벤트 수신 - memberId: {}",
			event.memberId());

		notificationCommandService.saveFromRankingEvent(event);
	}
}
