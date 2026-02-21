package com.ice.studyroom.domain.ranking.application.snapshot;

import com.ice.studyroom.domain.ranking.domain.entity.RankingSnapshot;
import com.ice.studyroom.domain.ranking.domain.entity.RankingSnapshotItem;
import com.ice.studyroom.domain.ranking.domain.type.RankingPeriod;
import com.ice.studyroom.domain.ranking.infrastructure.persistence.RankingSnapshotItemRepository;
import com.ice.studyroom.domain.ranking.infrastructure.persistence.RankingSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RankingSnapshotService {

	private final RankingSnapshotRepository rankingSnapshotRepository;
	private final RankingSnapshotItemRepository rankingSnapshotItemRepository;

	public void createSnapshot(
		RankingPeriod period,
		String periodKey,
		List<SnapshotData> snapshotDataList
	) {
		try {

			log.info("[RANKING] Snapshot DB 저장 시작 - period: {}, periodKey: {}, size: {}",
				period, periodKey, snapshotDataList.size());

			RankingSnapshot snapshot = rankingSnapshotRepository.save(
				RankingSnapshot.create(period, periodKey));

			log.info("[RANKING] Snapshot 엔티티 저장 완료 - snapshotId: {}",
				snapshot.getId());

			List<RankingSnapshotItem> items = snapshotDataList.stream()
				.map(data -> RankingSnapshotItem.create(
					snapshot.getId(),
					data.memberId,
					data.rank,
					data.score
				))
				.toList();

			rankingSnapshotItemRepository.saveAll(items);

			log.info("[RANKING] SnapshotItem 저장 완료 - snapshotId: {}, itemCount: {}",
				snapshot.getId(), items.size());

		} catch (Exception e) {

			log.error("[RANKING] ❌ Snapshot DB 저장 실패 - period: {}, periodKey: {}",
				period, periodKey, e);

			throw e;
		}
	}

	public record SnapshotData(
		Long memberId,
		int rank,
		int score
	) {}
}
