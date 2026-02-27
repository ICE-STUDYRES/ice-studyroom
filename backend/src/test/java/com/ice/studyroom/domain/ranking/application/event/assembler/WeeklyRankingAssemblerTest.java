package com.ice.studyroom.domain.ranking.application.event.assembler;

import com.ice.studyroom.domain.membership.domain.entity.Member;
import com.ice.studyroom.domain.membership.infrastructure.persistence.MemberRepository;
import com.ice.studyroom.domain.ranking.domain.service.RankingEntry;
import com.ice.studyroom.domain.ranking.domain.service.RankingStore;
import com.ice.studyroom.domain.ranking.domain.type.RankingPeriod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class WeeklyRankingAssemblerTest {

	private final RankingStore rankingStore = mock(RankingStore.class);
	private final MemberRepository memberRepository = mock(MemberRepository.class);

	private final WeeklyRankingAssembler assembler =
		new WeeklyRankingAssembler(rankingStore, memberRepository);

	@Test
	@DisplayName("Top5를 점수 기준으로 반환하고 이름을 마스킹한다")
	void buildTop5_success() {

		// given
		List<RankingEntry> entries = List.of(
			new RankingEntry(1L, 100),
			new RankingEntry(2L, 90),
			new RankingEntry(3L, 80)
		);

		when(rankingStore.getAllRankings(RankingPeriod.WEEKLY))
			.thenReturn(entries);

		Member m1 = mock(Member.class);
		Member m2 = mock(Member.class);
		Member m3 = mock(Member.class);

		when(m1.getId()).thenReturn(1L);
		when(m2.getId()).thenReturn(2L);
		when(m3.getId()).thenReturn(3L);

		when(m1.getName()).thenReturn("김예준");
		when(m2.getName()).thenReturn("박철수");
		when(m3.getName()).thenReturn("이영희");

		when(memberRepository.findAllById(List.of(1L,2L,3L)))
			.thenReturn(List.of(m1, m2, m3));

		// when
		var result = assembler.buildTop5(RankingPeriod.WEEKLY);

		// then
		assertThat(result).hasSize(3);
		assertThat(result.get(0).rank()).isEqualTo(1);
		assertThat(result.get(0).name()).isEqualTo("김*준");
		assertThat(result.get(0).score()).isEqualTo(100);
	}

	@Test
	@DisplayName("랭킹이 없으면 빈 리스트 반환")
	void buildTop5_empty() {

		when(rankingStore.getAllRankings(RankingPeriod.WEEKLY))
			.thenReturn(List.of());

		var result = assembler.buildTop5(RankingPeriod.WEEKLY);

		assertThat(result).isEmpty();
	}
}
