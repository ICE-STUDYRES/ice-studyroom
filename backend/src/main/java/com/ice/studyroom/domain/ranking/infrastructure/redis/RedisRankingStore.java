package com.ice.studyroom.domain.ranking.infrastructure.redis;

import com.ice.studyroom.domain.ranking.domain.service.RankingStore;
import com.ice.studyroom.domain.ranking.domain.type.RankingPeriod;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class RedisRankingStore implements RankingStore {

	private final StringRedisTemplate redisTemplate;

	private String key(RankingPeriod period) {
		return "ranking:" + period.name();
	}

	@Override
	public void increaseScore(RankingPeriod period, Long memberId, int score) {
		redisTemplate.opsForZSet()
			.incrementScore(key(period), memberId.toString(), score);
	}

	@Override
	public Integer getScore(RankingPeriod period, Long memberId) {
		Double score = redisTemplate.opsForZSet()
			.score(key(period), memberId.toString());

		return score == null ? null : score.intValue();
	}

	@Override
	public Integer getRank(RankingPeriod period, Long memberId) {
		Long rank = redisTemplate.opsForZSet()
			.reverseRank(key(period), memberId.toString());

		// redis는 0-based → 도메인은 1-based
		return rank == null ? null : rank.intValue() + 1;
	}

	@Override
	public Integer getUpperScore(RankingPeriod period, Long memberId) {

		Long currentRank = redisTemplate.opsForZSet()
			.reverseRank(key(period), memberId.toString());

		if (currentRank == null || currentRank == 0) {
			return null; // 랭킹 없음 or 1위
		}

		// 바로 위 순위 (score 기준으로 더 높은 점수)
		Set<ZSetOperations.TypedTuple<String>> upper =
			redisTemplate.opsForZSet()
				.reverseRangeWithScores(
					key(period),
					currentRank - 1,
					currentRank - 1
				);

		if (upper == null || upper.isEmpty()) {
			return null;
		}

		Double score = upper.iterator().next().getScore();
		return score == null ? null : score.intValue();
	}

	@Override
	public Integer getLowerScore(RankingPeriod period, Long memberId) {

		Long rank = redisTemplate.opsForZSet()
			.reverseRank(key(period), memberId.toString());

		if (rank == null) {
			return null;
		}

		// 바로 아래 순위 (1위면 2위)
		Set<ZSetOperations.TypedTuple<String>> lower =
			redisTemplate.opsForZSet()
				.reverseRangeWithScores(key(period), rank + 1, rank + 1);

		if (lower == null || lower.isEmpty()) {
			return null;
		}

		Double score = lower.iterator().next().getScore();

		return score == null ? null : score.intValue();
	}
}
