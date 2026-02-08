package com.ice.studyroom.domain.ranking.application.event;

public interface RankingEventPublisher {

	void publish(RankingEmailEvent event);
}
