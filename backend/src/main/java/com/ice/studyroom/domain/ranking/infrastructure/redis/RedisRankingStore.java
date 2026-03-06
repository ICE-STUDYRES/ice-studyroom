package com.ice.studyroom.domain.ranking.infrastructure.redis;

import com.ice.studyroom.domain.ranking.domain.service.RankingEntry;
import com.ice.studyroom.domain.ranking.domain.service.RankingStore;
import com.ice.studyroom.domain.ranking.domain.type.RankingPeriod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisRankingStore implements RankingStore {

	private final StringRedisTemplate redisTemplate;

	private String key(RankingPeriod period) {
		return "ranking:" + period.name();
	}

	@Override
	public void increaseScore(RankingPeriod period, Long memberId, int score) {
        try {
            redisTemplate.opsForZSet()
                .incrementScore(key(period), memberId.toString(), score);
        } catch (Exception e) {
			log.error("[RANKING] ❌ Redis 점수 증가 실패 - key: {}, memberId: {}, score: {}",
					key(period), memberId, score, e);

			throw e;
        }
    }

	@Override
	public Integer getScore(RankingPeriod period, Long memberId) {
		Double score = redisTemplate.opsForZSet()
			.score(key(period), memberId.toString());

		return score == null ? null : score.intValue();
	}

	@Override
	public Integer getUpperScore(RankingPeriod period, Long memberId) {

		Integer myScore = getScore(period, memberId);

		if (myScore == null) {
			return null;
		}

		// 점수가 높은 사람들 중에서
		Set<ZSetOperations.TypedTuple<String>> upper =
			redisTemplate.opsForZSet()
				.rangeByScoreWithScores(
					key(period),
					myScore + 1,
					Double.POSITIVE_INFINITY,
					0,
					1
				);

		if (upper == null || upper.isEmpty()) {
			return null;
		}

		Double score = upper.iterator().next().getScore();
		return score == null ? null : score.intValue();
	}

	@Override
	public Integer getRank(RankingPeriod period, Long memberId) {

        try {
            Integer myScore = getScore(period, memberId);
            if (myScore == null) return null;

            Long higherCount = redisTemplate.opsForZSet()
                    .count(key(period), myScore + 1, Double.POSITIVE_INFINITY);

            return higherCount.intValue() + 1;

        } catch (Exception e) {
			log.error("[RANKING] ❌ Redis 랭킹 조회 실패 - key: {}, memberId: {}",
					key(period), memberId, e);

			throw e;
        }
    }

	@Override
	public List<RankingEntry> getAllRankings(RankingPeriod period) {

        try {
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
        } catch (Exception e) {
			log.error("[RANKING] ❌ Redis 전체 랭킹 조회 실패 - key: {}",
					key(period), e);

			throw e;
        }
    }

	public void clear(RankingPeriod period) {
		redisTemplate.delete(key(period));
	}
}
