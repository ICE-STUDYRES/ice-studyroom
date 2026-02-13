package com.ice.studyroom.domain.ranking.application.snapshot;

import com.ice.studyroom.domain.ranking.domain.service.RankingEntry;
import com.ice.studyroom.domain.ranking.domain.service.RankingStore;
import com.ice.studyroom.domain.ranking.domain.type.RankingPeriod;
import com.ice.studyroom.domain.ranking.infrastructure.redis.RedisRankingStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RankingSnapshotJob {

	private final RankingStore rankingStore;
	private final RankingSnapshotService snapshotService;

	@Transactional
	public void execute(RankingPeriod period, String periodKey) {

		List<RankingEntry> entries =
			rankingStore.getAllRankings(period);

		List<RankingSnapshotService.SnapshotData> snapshotData =
			buildSnapshotData(entries);

		snapshotService.createSnapshot(period, periodKey, snapshotData);

		rankingStore.clear(period);
	}

	private List<RankingSnapshotService.SnapshotData> buildSnapshotData(
		List<RankingEntry> entries
	) {

		List<RankingSnapshotService.SnapshotData> result = new ArrayList<>();

		int previousScore = -1;
		int currentRank = 0;

		for (int i = 0; i < entries.size(); i++) {

			RankingEntry entry = entries.get(i);

			if (entry.score() != previousScore) {
				currentRank = i + 1;
				previousScore = entry.score();
			}

			result.add(
				new RankingSnapshotService.SnapshotData(
					entry.memberId(),
					currentRank,
					entry.score()
				)
			);
		}

		return result;
	}
}
