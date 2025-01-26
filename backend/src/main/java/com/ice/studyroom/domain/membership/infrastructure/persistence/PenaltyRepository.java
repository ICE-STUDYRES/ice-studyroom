package com.ice.studyroom.domain.membership.infrastructure.persistence;

import com.ice.studyroom.domain.membership.domain.entity.Penalty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface PenaltyRepository extends JpaRepository<Penalty, Long> {
	Long countByMemberIdAndPenaltyEndAfter(Long memberId, LocalDateTime currentTime);
}
