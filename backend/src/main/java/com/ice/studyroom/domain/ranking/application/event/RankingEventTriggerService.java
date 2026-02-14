package com.ice.studyroom.domain.ranking.application.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.ice.studyroom.domain.ranking.domain.service.RankingEventPolicy;

@Service
@RequiredArgsConstructor
public class RankingEventTriggerService {

	private final RankingEventPolicy rankingEventPolicy;
	private final RankingEventPublisher rankingEventPublisher;

	public void trigger(RankingContext context) {

		rankingEventPolicy
			.determine(context.previousRank(), context.currentRank())
			.ifPresent(eventType -> {
				RankingEmailEvent event = RankingEmailEvent.of(
					eventType,
					context.memberId(),
					context.memberName(),
					context.memberEmail(),
					context.currentRank(),
					context.previousRank(),
					context.gapWithUpper()
				);
				rankingEventPublisher.publish(event);
			});
	}
}
