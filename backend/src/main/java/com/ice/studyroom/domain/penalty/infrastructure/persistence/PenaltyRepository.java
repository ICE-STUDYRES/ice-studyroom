package com.ice.studyroom.domain.penalty.infrastructure.persistence;

import com.ice.studyroom.domain.penalty.domain.entity.Penalty;
import com.ice.studyroom.domain.penalty.domain.type.PenaltyStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface PenaltyRepository extends JpaRepository<Penalty, Long> {

	List<Penalty> findByStatus(PenaltyStatus status);

	Optional<Penalty> findByMemberIdAndStatus(Long memberId, PenaltyStatus status);
}
