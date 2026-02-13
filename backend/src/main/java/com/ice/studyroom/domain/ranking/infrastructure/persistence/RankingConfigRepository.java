package com.ice.studyroom.domain.ranking.infrastructure.persistence;

import com.ice.studyroom.domain.ranking.domain.entity.RankingConfig;
import com.ice.studyroom.domain.ranking.domain.type.RankingPeriod;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RankingConfigRepository extends JpaRepository<RankingConfig, Long> {

	RankingConfig findByPeriod(RankingPeriod period);
}
