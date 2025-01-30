package com.ice.studyroom.domain.membership.infrastructure.persistence;

import com.ice.studyroom.domain.membership.domain.entity.Penalty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PenaltyRepository extends JpaRepository<Penalty, Long> {
	Long countByMemberIdAndPenaltyEndAfter(Long memberId, LocalDateTime currentTime);
	List<Penalty> findByMemberIdAndPenaltyEndAfter(Long memberId, LocalDateTime currentTime);
}
