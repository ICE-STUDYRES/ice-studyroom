package com.ice.studyroom.domain.penalty.application;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.ice.studyroom.domain.membership.domain.entity.Member;
import com.ice.studyroom.domain.penalty.domain.entity.Penalty;
import com.ice.studyroom.domain.penalty.domain.service.PenaltyDomainService;
import com.ice.studyroom.domain.penalty.domain.type.PenaltyReasonType;
import com.ice.studyroom.domain.penalty.infrastructure.persistence.PenaltyRepository;
import com.ice.studyroom.domain.reservation.domain.entity.Reservation;
import com.ice.studyroom.domain.reservation.infrastructure.persistence.ReservationRepository;
import com.ice.studyroom.global.exception.BusinessException;
import com.ice.studyroom.global.type.StatusCode;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PenaltyService {

	private final PenaltyDomainService penaltyDomainService;
	private final PenaltyRepository penaltyRepository;
	private final ReservationRepository reservationRepository;

	@Transactional
	public void assignPenalty(Member member, Long reservationId, PenaltyReasonType reason) {
		Reservation reservation = reservationRepository.findById(reservationId).
			orElseThrow(() -> new BusinessException(StatusCode.NOT_FOUND, "예약 정보를 찾을 수 없습니다."));

		Penalty penalty = penaltyDomainService.createPenalty(member, reservation, reason, null);
		member.updatePenalty(true);
		penaltyRepository.save(penalty);
	}

	public void adminAssignPenalty(Member member, LocalDateTime penaltyEndAt) {
		Penalty penalty = penaltyDomainService.createPenalty(member, null, PenaltyReasonType.ADMIN, penaltyEndAt);
		member.updatePenalty(true);
		penaltyRepository.save(penalty);
	}

	public void adminDeletePenalty(Member member) {
		Penalty penalty = penaltyDomainService.findPenaltyByMemberIdAndStatus(member);
		penalty.expirePenalty();
		member.updatePenalty(false);
	}
}
