package com.ice.studyroom.domain.ranking.domain.service;

import com.ice.studyroom.domain.ranking.application.event.RankingEventType;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RankingEventPolicy {

	public Optional<RankingEventType> determine(int previousRank, int currentRank) {
		if (previousRank <= 5 || currentRank <= 5) {
			return Optional.of(RankingEventType.TOP5_RANK_CHANGED);
		}

		if (previousRank <= 10 || currentRank <= 10) {
			return Optional.of(RankingEventType.TOP6_10_RANK_CHANGED);
		}

		return Optional.empty();
	}
}
