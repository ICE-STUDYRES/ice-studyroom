package com.ice.studyroom.domain.ranking.infrastructure.redis;

import static org.assertj.core.api.Assertions.*;

import com.ice.studyroom.domain.ranking.domain.type.RankingPeriod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RedisRankingStoreTest {

	@Autowired
	private RedisRankingStore redisRankingStore;

	private final RankingPeriod PERIOD = RankingPeriod.WEEKLY;

	@BeforeEach
	void setUp() {
		// 테스트 전에 키 초기화
		redisRankingStore.clear(PERIOD);
	}

	@Test
	void 점수_증가_및_랭크_확인() {

		redisRankingStore.increaseScore(PERIOD, 1L, 10);
		redisRankingStore.increaseScore(PERIOD, 2L, 20);

		Integer rank1 = redisRankingStore.getRank(PERIOD, 1L);
		Integer rank2 = redisRankingStore.getRank(PERIOD, 2L);

		assertThat(rank2).isEqualTo(1);
		assertThat(rank1).isEqualTo(2);
	}

	@Test
	void 동점자_정렬_기본_검증() {

		redisRankingStore.clear(PERIOD);

		redisRankingStore.increaseScore(PERIOD, 2L, 50);
		redisRankingStore.increaseScore(PERIOD, 10L, 50);

		Integer rank2 = redisRankingStore.getRank(PERIOD, 2L);
		Integer rank10 = redisRankingStore.getRank(PERIOD, 10L);

		assertThat(rank2).isEqualTo(1);
		assertThat(rank10).isEqualTo(2);
	}

	@Test
	void 동점자_정렬_확인() {

		redisRankingStore.increaseScore(PERIOD, 2L, 20);
		redisRankingStore.increaseScore(PERIOD, 10L, 20);

		Integer rank2 = redisRankingStore.getRank(PERIOD, 2L);
		Integer rank10 = redisRankingStore.getRank(PERIOD, 10L);

		System.out.println("2L rank = " + rank2);
		System.out.println("10L rank = " + rank10);

		assertThat(rank2).isNotEqualTo(rank10);
	}

	@Test
	void 동점자_3명_정렬_검증() {

		redisRankingStore.clear(PERIOD);

		redisRankingStore.increaseScore(PERIOD, 30L, 100);
		redisRankingStore.increaseScore(PERIOD, 2L, 100);
		redisRankingStore.increaseScore(PERIOD, 15L, 100);

		assertThat(redisRankingStore.getRank(PERIOD, 2L)).isEqualTo(1);
		assertThat(redisRankingStore.getRank(PERIOD, 15L)).isEqualTo(2);
		assertThat(redisRankingStore.getRank(PERIOD, 30L)).isEqualTo(3);
	}

	@Test
	void 동점자_상위점수존재_케이스() {

		redisRankingStore.clear(PERIOD);

		// 1위
		redisRankingStore.increaseScore(PERIOD, 99L, 200);

		// 동점 그룹
		redisRankingStore.increaseScore(PERIOD, 3L, 100);
		redisRankingStore.increaseScore(PERIOD, 1L, 100);
		redisRankingStore.increaseScore(PERIOD, 2L, 100);

		assertThat(redisRankingStore.getRank(PERIOD, 99L)).isEqualTo(1);

		assertThat(redisRankingStore.getRank(PERIOD, 1L)).isEqualTo(2);
		assertThat(redisRankingStore.getRank(PERIOD, 2L)).isEqualTo(3);
		assertThat(redisRankingStore.getRank(PERIOD, 3L)).isEqualTo(4);
	}

	@Test
	void 동점상태에서_한명_점수상승시_재정렬() {

		redisRankingStore.clear(PERIOD);

		redisRankingStore.increaseScore(PERIOD, 1L, 100);
		redisRankingStore.increaseScore(PERIOD, 2L, 100);

		// 동점 → 1L이 1위
		assertThat(redisRankingStore.getRank(PERIOD, 1L)).isEqualTo(1);
		assertThat(redisRankingStore.getRank(PERIOD, 2L)).isEqualTo(2);

		// 2L 점수 +1
		redisRankingStore.increaseScore(PERIOD, 2L, 1);

		assertThat(redisRankingStore.getRank(PERIOD, 2L)).isEqualTo(1);
		assertThat(redisRankingStore.getRank(PERIOD, 1L)).isEqualTo(2);
	}

	@Test
	void 동점자_getUpperScore_검증() {

		redisRankingStore.clear(PERIOD);

		redisRankingStore.increaseScore(PERIOD, 1L, 100);
		redisRankingStore.increaseScore(PERIOD, 2L, 100);
		redisRankingStore.increaseScore(PERIOD, 3L, 90);

		// 2L은 1L과 동점 → 위 점수는 100이어야 함
		Integer upperScore = redisRankingStore.getUpperScore(PERIOD, 2L);

		assertThat(upperScore).isEqualTo(100);
	}
}
