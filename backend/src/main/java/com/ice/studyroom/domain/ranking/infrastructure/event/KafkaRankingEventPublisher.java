package com.ice.studyroom.domain.ranking.infrastructure.event;

import com.ice.studyroom.domain.ranking.application.event.dto.RankingListUpdatedEvent;
import com.ice.studyroom.domain.ranking.application.event.dto.RankingUserChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.ice.studyroom.domain.ranking.application.event.publisher.RankingEventPublisher;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaRankingEventPublisher implements RankingEventPublisher {

	private final KafkaTemplate<String, Object> kafkaTemplate;

	private static final String USER_TOPIC = "RANKING_USER_CHANGED_EVENT";
	private static final String LIST_TOPIC = "RANKING_LIST_UPDATED_EVENT";

	@Override
	public void publishUserChanged(RankingUserChangedEvent event) {

		kafkaTemplate.send(USER_TOPIC,
						event.memberId().toString(),
						event
				).whenComplete((result, ex) -> {

					if (ex != null) {
						log.error("[RANKING] ❌ USER_CHANGED_EVENT 전송 실패 - eventId: {}",
								event.eventId(), ex);
					} else {
						log.info("[RANKING] ✅ USER_CHANGED_EVENT 전송 성공 - eventId: {}",
								event.eventId());
					}
				});
	}

	@Override
	public void publishListUpdated(RankingListUpdatedEvent event) {

		kafkaTemplate.send(LIST_TOPIC,
						event.periodKey(),
						event
				).whenComplete((result, ex) -> {

					if (ex != null) {
						log.error("[RANKING] ❌ LIST_UPDATED_EVENT 전송 실패 - eventId: {}",
								event.eventId(), ex);
					} else {
						log.info("[RANKING] ✅ LIST_UPDATED_EVENT 전송 성공 - eventId: {}",
								event.eventId());
					}
				});
	}
}
