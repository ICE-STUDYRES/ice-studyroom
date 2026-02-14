package com.ice.studyroom.domain.ranking.application.snapshot;

import com.ice.studyroom.domain.ranking.domain.service.RankingEntry;
import com.ice.studyroom.domain.ranking.domain.service.RankingStore;
import com.ice.studyroom.domain.ranking.domain.type.RankingPeriod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;


import java.util.List;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RankingSnapshotJobUnitTest {

	@Mock
	private RankingStore rankingStore;

	@Mock
	private RankingSnapshotService snapshotService;

	@InjectMocks
	private RankingSnapshotJob snapshotJob;

	private final RankingPeriod PERIOD = RankingPeriod.WEEKLY;

	@Test
	void 공동순위_정상_계산_및_스냅샷_저장_검증() {

		// given
		List<RankingEntry> entries = List.of(
			new RankingEntry(1L, 100),
			new RankingEntry(2L, 100),
			new RankingEntry(3L, 80)
		);

		given(rankingStore.getAllRankings(PERIOD))
			.willReturn(entries);

		// when
		snapshotJob.execute(PERIOD, "2026-02-16");

		// then
		verify(snapshotService).createSnapshot(
			eq(PERIOD),
			eq("2026-02-16"),
			argThat(snapshotDataList -> {

				if (snapshotDataList.size() != 3) return false;

				var m1 = snapshotDataList.stream()
					.filter(d -> d.memberId().equals(1L))
					.findFirst().get();

				var m2 = snapshotDataList.stream()
					.filter(d -> d.memberId().equals(2L))
					.findFirst().get();

				var m3 = snapshotDataList.stream()
					.filter(d -> d.memberId().equals(3L))
					.findFirst().get();

				return m1.rank() == 1 &&
					m2.rank() == 1 &&
					m3.rank() == 3;
			})
		);

		verify(rankingStore).clear(PERIOD);
	}

	@Test
	void 전원_동점_테스트() {

		List<RankingEntry> entries = List.of(
			new RankingEntry(1L, 100),
			new RankingEntry(2L, 100),
			new RankingEntry(3L, 100)
		);

		given(rankingStore.getAllRankings(PERIOD))
			.willReturn(entries);

		snapshotJob.execute(PERIOD, "2026-02-16");

		verify(snapshotService).createSnapshot(
			eq(PERIOD),
			eq("2026-02-16"),
			argThat(list ->
				list.stream().allMatch(d -> d.rank() == 1)
			)
		);
	}

	@Test
	void 한명만_있는_경우() {

		List<RankingEntry> entries = List.of(
			new RankingEntry(1L, 50)
		);

		given(rankingStore.getAllRankings(PERIOD))
			.willReturn(entries);

		snapshotJob.execute(PERIOD, "2026-02-16");

		verify(snapshotService).createSnapshot(
			eq(PERIOD),
			eq("2026-02-16"),
			argThat(list ->
				list.size() == 1 &&
					list.get(0).rank() == 1
			)
		);
	}

	@Test
	void 빈랭킹도_snapshot_생성() {

		given(rankingStore.getAllRankings(PERIOD))
			.willReturn(List.of());

		snapshotJob.execute(PERIOD, "2026-02-16");

		verify(snapshotService).createSnapshot(
			eq(PERIOD),
			eq("2026-02-16"),
			eq(List.of())
		);

		verify(rankingStore).clear(PERIOD);
	}

	@Test
	void 스냅샷_후_clear는_항상_호출된다() {

		List<RankingEntry> entries = List.of(
			new RankingEntry(1L, 10)
		);

		given(rankingStore.getAllRankings(PERIOD))
			.willReturn(entries);

		snapshotJob.execute(PERIOD, "2026-02-16");

		verify(rankingStore).clear(PERIOD);
	}

	@Test
	void 스냅샷_저장_실패시_Redis_clear_호출되지_않는다() {

		// given
		List<RankingEntry> entries = List.of(
			new RankingEntry(1L, 100)
		);

		given(rankingStore.getAllRankings(PERIOD))
			.willReturn(entries);

		// snapshotService에서 예외 발생하도록 설정
		doThrow(new RuntimeException("DB 저장 실패"))
			.when(snapshotService)
			.createSnapshot(eq(PERIOD), eq("2026-02-16"), any());

		// when
		try {
			snapshotJob.execute(PERIOD, "2026-02-16");
		} catch (RuntimeException ignored) {
		}

		// then
		verify(snapshotService).createSnapshot(
			eq(PERIOD),
			eq("2026-02-16"),
			any()
		);

		// clear는 호출되면 안 됨
		verify(rankingStore, never()).clear(PERIOD);
	}

}
