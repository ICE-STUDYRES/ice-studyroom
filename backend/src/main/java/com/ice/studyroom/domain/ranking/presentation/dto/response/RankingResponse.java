package com.ice.studyroom.domain.ranking.presentation;

public record RankingResponse(

	int rank,
	String name,
	int score
) {}
