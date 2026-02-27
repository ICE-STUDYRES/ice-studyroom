package com.ice.studyroom.domain.ranking.application.event.dto;

public record WeeklyRankingDto(

	int rank,
	String name,
	int score
) {}
