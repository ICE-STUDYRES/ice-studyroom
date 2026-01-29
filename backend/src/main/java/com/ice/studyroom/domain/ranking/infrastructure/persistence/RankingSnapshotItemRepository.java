package com.ice.studyroom.domain.ranking.infrastructure.persistence;

import com.ice.studyroom.domain.ranking.domain.entity.RankingSnapshotItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RankingSnapshotItemRepository extends JpaRepository<RankingSnapshotItem, Long> {
}
