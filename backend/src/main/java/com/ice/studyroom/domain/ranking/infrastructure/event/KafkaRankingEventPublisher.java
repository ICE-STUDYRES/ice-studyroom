package com.ice.studyroom.domain.ranking.infrastructure.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Component;

import com.ice.studyroom.domain.ranking.application.event.RankingEmailEvent;
import com.ice.studyroom.domain.ranking.application.event.RankingEventPublisher;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaRankingEventPublisher implements RankingEventPublisher {

	private final KafkaTemplate<String, RankingEmailEvent> kafkaTemplate;

	private static final String TOPIC = "ranking.mail";

	@Override
	public void publish(RankingEmailEvent event) {
		kafkaTemplate.send(TOPIC, event)
			.whenComplete((result, ex) -> {

				if (ex != null) {

					log.error("[RANKING] ❌ Kafka 전송 실패 - topic: {}, eventId: {}",
						TOPIC,
						event.eventId(),
						ex);

				} else {

					log.info("[RANKING] ✅ Kafka 전송 성공 - topic: {}, eventId: {}",
						TOPIC,
						event.eventId());
				}
			});
	}
}
