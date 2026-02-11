package com.ice.studyroom.domain.ranking.domain.service;

import com.ice.studyroom.domain.ranking.application.event.RankingEventType;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RankingEventPolicy {

	public Optional<RankingEventType> determine(int previousRank, int currentRank) {

		// 1. 순위가 변하지 않았으면 이벤트 없음
		if (previousRank == currentRank) {
			return Optional.empty();
		}

		// 2. Top5 구간 변화
		if (previousRank <= 5 || currentRank <= 5) {
			return Optional.of(RankingEventType.TOP5_RANK_CHANGED);
		}

		// 3. Top6~10 구간 변화
		if (previousRank <= 10 || currentRank <= 10) {
			return Optional.of(RankingEventType.TOP6_10_RANK_CHANGED);
		}

		return Optional.empty();
	}
}
