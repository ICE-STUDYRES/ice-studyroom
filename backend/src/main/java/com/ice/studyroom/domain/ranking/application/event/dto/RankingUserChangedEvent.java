package com.ice.studyroom.domain.ranking.application.event.dto;

import com.ice.studyroom.domain.ranking.application.event.policy.RankingEventType;
import com.ice.studyroom.domain.ranking.application.event.publisher.EventIdGenerator;

public record RankingUserChangedEvent(

	String eventId,
	RankingEventType eventType,
	String periodKey,

	Long memberId,
	String name,
	String email,

	int currentRank,
	Integer previousRank,
	int score,
	Integer gapWithUpper
) {

	public static RankingUserChangedEvent of(
		EventIdGenerator generator,
		RankingEventType eventType,
		String periodKey,
		Long memberId,
		String name,
		String email,
		int currentRank,
		Integer previousRank,
		int score,
		Integer gapWithUpper
	) {

		return new RankingUserChangedEvent(
			generator.generate(periodKey),
			eventType,
			periodKey,
			memberId,
			name,
			email,
			currentRank,
			previousRank,
			score,
			gapWithUpper
		);
	}
}
