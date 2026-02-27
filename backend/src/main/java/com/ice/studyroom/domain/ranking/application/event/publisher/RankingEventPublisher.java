package com.ice.studyroom.domain.ranking.application.event;

import com.ice.studyroom.domain.ranking.application.event.dto.RankingListUpdatedEvent;
import com.ice.studyroom.domain.ranking.application.event.dto.RankingUserChangedEvent;

public interface RankingEventPublisher {

	void publishUserChanged(RankingUserChangedEvent event);

	void publishListUpdated(RankingListUpdatedEvent event);
}
