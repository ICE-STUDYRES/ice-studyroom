package com.ice.studyroom.domain.ranking.domain.service;

import com.ice.studyroom.domain.ranking.domain.type.RankingPeriod;

public interface RankingStore {

	/**
	 * 점수 증가 (ZINCRBY)
	 */
	void increaseScore(RankingPeriod period, Long memberId, int score);

	/**
	 * 현재 점수 조회 (ZSCORE)
	 */
	Integer getScore(RankingPeriod period, Long memberId);

	/**
	 * 바로 위 순위 사용자 점수 조회
	 * - 1위거나 랭킹 외면 null
	 */
	Integer getUpperScore(RankingPeriod period, Long memberId);

	/**
	 * Competition Rank 기준 공동 순위 조회
	 * (동점자는 동일 순위를 가지며, 다음 순위는 건너뛴다)
	 */
	Integer getRank(RankingPeriod period, Long memberId);

}
