package com.ice.studyroom.domain.ranking.infrastructure.persistence;

import com.ice.studyroom.domain.ranking.domain.entity.RankingSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RankingSnapshotRepository extends JpaRepository<RankingSnapshot, Long> {
}
