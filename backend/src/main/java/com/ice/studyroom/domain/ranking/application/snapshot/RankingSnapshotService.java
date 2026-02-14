package com.ice.studyroom.domain.ranking.application.snapshot;

import com.ice.studyroom.domain.ranking.domain.entity.RankingSnapshot;
import com.ice.studyroom.domain.ranking.domain.entity.RankingSnapshotItem;
import com.ice.studyroom.domain.ranking.domain.type.RankingPeriod;
import com.ice.studyroom.domain.ranking.infrastructure.persistence.RankingSnapshotItemRepository;
import com.ice.studyroom.domain.ranking.infrastructure.persistence.RankingSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
		RankingSnapshot snapshot = rankingSnapshotRepository.save(
			RankingSnapshot.create(period, periodKey));

		List<RankingSnapshotItem> items = snapshotDataList.stream()
			.map(data -> RankingSnapshotItem.create(
				snapshot.getId(),
				data.memberId,
				data.rank,
				data.score
			))
			.toList();

		rankingSnapshotItemRepository.saveAll(items);
	}

	public record SnapshotData(
		Long memberId,
		int rank,
		int score
	) {}
}
