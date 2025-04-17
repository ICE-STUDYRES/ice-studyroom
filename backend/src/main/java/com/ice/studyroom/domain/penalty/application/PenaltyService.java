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
		log.info("패널티 부여 요청 - userId: {}, reservationId: {}, reason: {}", member.getId(), reservationId, reason);
		Reservation reservation = reservationRepository.findById(reservationId).
			orElseThrow(() -> {
				log.warn("패널티 조회 실패 - 유효한 패널티 없음 - userId: {}", member.getId());
				return new BusinessException(StatusCode.NOT_FOUND, "예약 정보를 찾을 수 없습니다.");
			});

		Penalty penalty = penaltyDomainService.createPenalty(member, reservation, reason, null);
		member.updatePenalty(true);
		penaltyRepository.save(penalty);
		log.info("패널티 저장 완료 - penaltyId: {}", penalty.getId());
	}

	public void adminAssignPenalty(Member member, LocalDateTime penaltyEndAt) {
		log.info("관리자 패널티 수동 부여 - userId: {}, 종료일시: {}", member.getId(), penaltyEndAt);
		Penalty penalty = penaltyDomainService.createPenalty(member, null, PenaltyReasonType.ADMIN, penaltyEndAt);
		member.updatePenalty(true);
		penaltyRepository.save(penalty);
	}

	public void adminDeletePenalty(Member member) {
		log.info("관리자 패널티 삭제 요청 - userId: {}", member.getId());
		Penalty penalty = penaltyDomainService.findPenaltyByMemberIdAndStatus(member);
		penalty.expirePenalty();
		member.updatePenalty(false);
	}
}
