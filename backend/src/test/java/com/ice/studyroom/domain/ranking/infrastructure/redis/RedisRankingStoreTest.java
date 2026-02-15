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
		redisRankingStore.clear(PERIOD);
	}

	// 1️⃣ 기본 순위 검증
	@Test
	void 점수_증가_및_공동순위_확인() {

		redisRankingStore.increaseScore(PERIOD, 1L, 10);
		redisRankingStore.increaseScore(PERIOD, 2L, 20);

		assertThat(redisRankingStore.getRank(PERIOD, 2L)).isEqualTo(1);
		assertThat(redisRankingStore.getRank(PERIOD, 1L)).isEqualTo(2);
	}

	// 2️⃣ 동점자 → 공동 1등
	@Test
	void 동점자_공동1등_검증() {

		redisRankingStore.increaseScore(PERIOD, 2L, 50);
		redisRankingStore.increaseScore(PERIOD, 10L, 50);

		assertThat(redisRankingStore.getRank(PERIOD, 2L)).isEqualTo(1);
		assertThat(redisRankingStore.getRank(PERIOD, 10L)).isEqualTo(1);
	}

	// 3️⃣ 동점자 3명 → 모두 1등
	@Test
	void 동점자_3명_모두_1등() {

		redisRankingStore.increaseScore(PERIOD, 30L, 100);
		redisRankingStore.increaseScore(PERIOD, 2L, 100);
		redisRankingStore.increaseScore(PERIOD, 15L, 100);

		assertThat(redisRankingStore.getRank(PERIOD, 2L)).isEqualTo(1);
		assertThat(redisRankingStore.getRank(PERIOD, 15L)).isEqualTo(1);
		assertThat(redisRankingStore.getRank(PERIOD, 30L)).isEqualTo(1);
	}

	// 4️⃣ 상위 점수 존재 + 동점 그룹 → 다음은 4등
	@Test
	void 공동1등_3명_다음은_4등() {

		// 공동 1등
		redisRankingStore.increaseScore(PERIOD, 1L, 100);
		redisRankingStore.increaseScore(PERIOD, 2L, 100);
		redisRankingStore.increaseScore(PERIOD, 3L, 100);

		// 그 다음 점수
		redisRankingStore.increaseScore(PERIOD, 4L, 90);

		assertThat(redisRankingStore.getRank(PERIOD, 1L)).isEqualTo(1);
		assertThat(redisRankingStore.getRank(PERIOD, 2L)).isEqualTo(1);
		assertThat(redisRankingStore.getRank(PERIOD, 3L)).isEqualTo(1);

		assertThat(redisRankingStore.getRank(PERIOD, 4L)).isEqualTo(4);
	}

	// 5️⃣ 1 2 2 2 8 형태 검증
	@Test
	void rank_1_2_2_2_8_형태_검증() {

		// 1등
		redisRankingStore.increaseScore(PERIOD, 99L, 200);

		// 공동 2등 (3명)
		redisRankingStore.increaseScore(PERIOD, 1L, 100);
		redisRankingStore.increaseScore(PERIOD, 2L, 100);
		redisRankingStore.increaseScore(PERIOD, 3L, 100);

		// 그 다음
		redisRankingStore.increaseScore(PERIOD, 4L, 50);

		assertThat(redisRankingStore.getRank(PERIOD, 99L)).isEqualTo(1);

		assertThat(redisRankingStore.getRank(PERIOD, 1L)).isEqualTo(2);
		assertThat(redisRankingStore.getRank(PERIOD, 2L)).isEqualTo(2);
		assertThat(redisRankingStore.getRank(PERIOD, 3L)).isEqualTo(2);

		assertThat(redisRankingStore.getRank(PERIOD, 4L)).isEqualTo(5);
	}

	// 6️⃣ 동점 상태에서 한 명 점수 상승
	@Test
	void 동점상태에서_한명_점수상승시_재정렬() {

		redisRankingStore.increaseScore(PERIOD, 1L, 100);
		redisRankingStore.increaseScore(PERIOD, 2L, 100);

		assertThat(redisRankingStore.getRank(PERIOD, 1L)).isEqualTo(1);
		assertThat(redisRankingStore.getRank(PERIOD, 2L)).isEqualTo(1);

		// 2번이 1점 추가
		redisRankingStore.increaseScore(PERIOD, 2L, 1);

		assertThat(redisRankingStore.getRank(PERIOD, 2L)).isEqualTo(1);
		assertThat(redisRankingStore.getRank(PERIOD, 1L)).isEqualTo(2);
	}

	// 7️⃣ upperScore 검증 (동점일 경우 null)
	@Test
	void upperScore_동점일경우_null() {

		redisRankingStore.increaseScore(PERIOD, 1L, 100);
		redisRankingStore.increaseScore(PERIOD, 2L, 100);
		redisRankingStore.increaseScore(PERIOD, 3L, 90);

		// 2L은 공동 1등 → 위 점수 없음
		assertThat(redisRankingStore.getUpperScore(PERIOD, 2L)).isNull();

		// 3L은 위 점수 100
		assertThat(redisRankingStore.getUpperScore(PERIOD, 3L)).isEqualTo(100);
	}
}
