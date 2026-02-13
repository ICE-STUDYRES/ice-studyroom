package com.ice.studyroom.domain.ranking.domain.service;

import com.ice.studyroom.domain.ranking.application.event.RankingEventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class RankingEventPolicyTest {

	private RankingEventPolicy policy;

	@BeforeEach
	void setUp() {
		policy = new RankingEventPolicy();
	}

	@Test
	@DisplayName("6 → 5 : TOP5_RANK_CHANGED")
	void determine_top5_enter() {
		Optional<RankingEventType> result = policy.determine(6, 5);

		assertThat(result)
			.isPresent()
			.contains(RankingEventType.TOP5_RANK_CHANGED);
	}

	@Test
	@DisplayName("3 → 2 : TOP5_RANK_CHANGED")
	void determine_top5_internal_change() {
		Optional<RankingEventType> result = policy.determine(3, 2);

		assertThat(result)
			.isPresent()
			.contains(RankingEventType.TOP5_RANK_CHANGED);
	}

	@Test
	@DisplayName("5 → 6 : TOP5_RANK_CHANGED")
	void determine_top5_exit() {
		Optional<RankingEventType> result = policy.determine(5, 6);

		assertThat(result)
			.isPresent()
			.contains(RankingEventType.TOP5_RANK_CHANGED);
	}

	@Test
	@DisplayName("11 → 9 : TOP6_10_RANK_CHANGED")
	void determine_top6_10_enter() {
		Optional<RankingEventType> result = policy.determine(11, 9);

		assertThat(result)
			.isPresent()
			.contains(RankingEventType.TOP6_10_RANK_CHANGED);
	}

	@Test
	@DisplayName("9 → 11 : TOP6_10_RANK_CHANGED")
	void determine_top6_10_exit() {
		Optional<RankingEventType> result = policy.determine(9, 11);

		assertThat(result)
			.isPresent()
			.contains(RankingEventType.TOP6_10_RANK_CHANGED);
	}

	@Test
	@DisplayName("3 → 3 : 순위 변화 없음 → 이벤트 없음")
	void determine_no_change() {
		Optional<RankingEventType> result = policy.determine(3, 3);

		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("15 → 14 : 10위 밖 변화 → 이벤트 없음")
	void determine_outside_range() {
		Optional<RankingEventType> result = policy.determine(15, 14);

		assertThat(result).isEmpty();
	}
}
