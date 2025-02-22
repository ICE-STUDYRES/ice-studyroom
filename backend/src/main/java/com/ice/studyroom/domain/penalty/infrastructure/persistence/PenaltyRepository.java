package com.ice.studyroom.domain.penalty.infrastructure.persistence;

import com.ice.studyroom.domain.penalty.domain.entity.Penalty;
import com.ice.studyroom.domain.penalty.domain.type.PenaltyStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import io.lettuce.core.dynamic.annotation.Param;

public interface PenaltyRepository extends JpaRepository<Penalty, Long> {
	Optional<Penalty> findTopByMemberIdAndPenaltyEndAfterOrderByPenaltyEndDesc(Long memberId, LocalDateTime currentTime);

	List<Penalty> findByMemberIdAndPenaltyEndAfter(Long memberId, LocalDateTime currentTime);

	List<Penalty> findByStatus(PenaltyStatus status);
}
