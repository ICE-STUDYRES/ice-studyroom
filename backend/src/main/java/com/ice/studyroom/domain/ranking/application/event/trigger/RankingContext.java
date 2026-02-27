package com.ice.studyroom.domain.ranking.application.event.trigger;

public record RankingContext(

	Long memberId,
	String memberName,
	String memberEmail,

	int previousRank,
	int currentRank,

	Integer gapWithUpper
) {
}
