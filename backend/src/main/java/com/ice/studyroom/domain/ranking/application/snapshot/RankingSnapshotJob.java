package com.ice.studyroom.domain.ranking.application.snapshot;

import com.ice.studyroom.domain.ranking.domain.service.RankingEntry;
import com.ice.studyroom.domain.ranking.domain.service.RankingStore;
import com.ice.studyroom.domain.ranking.domain.type.RankingPeriod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingSnapshotJob {

	private final RankingStore rankingStore;
	private final RankingSnapshotService snapshotService;

	@Transactional
	public void execute(RankingPeriod period, String periodKey) {

		log.info("[RANKING] Snapshot 시작 - period: {}, periodKey: {}",
				period, periodKey);

        try {

            List<RankingEntry> entries =
                rankingStore.getAllRankings(period);

			log.info("[RANKING] Snapshot 대상 건수 - period: {}, count: {}",
					period, entries.size());

			if (entries.isEmpty()) {
				log.warn("[RANKING] Snapshot 대상 없음 - period: {}", period);
			}

            List<RankingSnapshotService.SnapshotData> snapshotData =
                buildSnapshotData(entries);

            snapshotService.createSnapshot(period, periodKey, snapshotData);

			log.info("[RANKING] Snapshot 저장 완료 - period: {}, periodKey: {}",
					period, periodKey);

            rankingStore.clear(period);

			log.info("[RANKING] Redis 초기화 완료 - period: {}",
					period);

        } catch (Exception e) {
			log.error("[RANKING] ❌ Snapshot 실패 - period: {}, periodKey: {}",
					period, periodKey, e);

			throw e;
        }
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
