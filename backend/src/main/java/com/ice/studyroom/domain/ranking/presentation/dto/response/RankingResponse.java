package com.ice.studyroom.domain.ranking.presentation.dto.response;

public record RankingResponse(

	int rank,
	String name,
	int score
) {}
