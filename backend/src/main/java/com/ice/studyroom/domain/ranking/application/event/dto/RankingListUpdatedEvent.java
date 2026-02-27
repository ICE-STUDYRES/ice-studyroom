package com.ice.studyroom.domain.ranking.application.event.dto;

import com.ice.studyroom.domain.ranking.application.event.publisher.EventIdGenerator;

import java.util.List;

public record RankingListUpdatedEvent(

	String eventId,
	String periodKey,
	List<WeeklyRankingDto> rankings
) {

	public static RankingListUpdatedEvent of(
		EventIdGenerator generator,
		String periodKey,
		List<WeeklyRankingDto> rankings
	) {
		return new RankingListUpdatedEvent(
			generator.generate(periodKey),
			periodKey,
			rankings
		);
	}
}
