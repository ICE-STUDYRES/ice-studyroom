package com.ice.studyroom.domain.ranking.infrastructure.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.ice.studyroom.domain.ranking.application.event.RankingEmailEvent;
import com.ice.studyroom.domain.ranking.application.event.RankingEventPublisher;

@Slf4j
@Component
public class DummyRankingEventPublisher implements RankingEventPublisher {

	@Override
	public void publish(RankingEmailEvent event) {
		log.info("[DummyRankingEventPublisher] event published: {}", event);
	}
}
