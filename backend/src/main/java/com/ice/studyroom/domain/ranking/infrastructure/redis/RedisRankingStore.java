package com.ice.studyroom.domain.ranking.infrastructure.redis;

import com.ice.studyroom.domain.ranking.domain.service.RankingEntry;
import com.ice.studyroom.domain.ranking.domain.service.RankingStore;
import com.ice.studyroom.domain.ranking.domain.type.RankingPeriod;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.List;
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
	public Integer getRank(RankingPeriod period, Long memberId) {

		Integer myScore = getScore(period, memberId);
		if (myScore == null) return null;

		Long higherCount = redisTemplate.opsForZSet()
				.count(key(period), myScore + 1, Double.POSITIVE_INFINITY);

		return higherCount.intValue() + 1;
	}

	@Override
	public List<RankingEntry> getAllRankings(RankingPeriod period) {

		Set<ZSetOperations.TypedTuple<String>> tuples =
				redisTemplate.opsForZSet()
						.reverseRangeWithScores(key(period), 0, -1);

		if (tuples == null || tuples.isEmpty()) {
			return List.of();
		}

		return tuples.stream()
				.map(tuple -> new RankingEntry(
						Long.valueOf(tuple.getValue()),
						tuple.getScore().intValue()
				))
				.toList();
	}


	public void clear(RankingPeriod period) {
		redisTemplate.delete(key(period));
	}
}
