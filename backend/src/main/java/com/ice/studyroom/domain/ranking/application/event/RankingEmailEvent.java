package com.ice.studyroom.domain.ranking.application.event;

import java.util.UUID;

public record RankingEmailEvent(

	String eventId,
	RankingEventType eventType,

	Long memberId,
	String name,
	String email,

	int currentRank,
	int previousRank,

	Integer gapWithUpper // 알림 판단을 위한 정보
) {

	public static RankingEmailEvent of(
		RankingEventType eventType,
		Long memberId,
		String name,
		String email,
		int currentRank,
		int previousRank,
		Integer gapWithUpper
	) {
		return new RankingEmailEvent(
			UUID.randomUUID().toString(),
			eventType,
			memberId,
			name,
			email,
			currentRank,
			previousRank,
			gapWithUpper
		);
	}
}
