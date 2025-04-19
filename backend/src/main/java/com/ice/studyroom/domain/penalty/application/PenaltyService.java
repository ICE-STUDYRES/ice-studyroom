package com.ice.studyroom.domain.penalty.application;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.ice.studyroom.domain.membership.domain.entity.Member;
import com.ice.studyroom.domain.penalty.domain.entity.Penalty;
import com.ice.studyroom.domain.penalty.domain.service.PenaltyDomainService;
import com.ice.studyroom.domain.penalty.domain.type.PenaltyReasonType;
import com.ice.studyroom.domain.penalty.domain.util.PenaltyLogUtil;
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
		PenaltyLogUtil.log("패널티 부여 요청", "학번: " + member.getStudentNum(), "reservationId: " + reservationId, "reason: " + reason);
		Reservation reservation = reservationRepository.findById(reservationId).
			orElseThrow(() -> {
				PenaltyLogUtil.logWarn("패널티 부여 실패 - 존재하지 않는 예약", "reservationId: " + reservationId);
				return new BusinessException(StatusCode.NOT_FOUND, "예약 정보를 찾을 수 없습니다.");
			});

		Penalty penalty = penaltyDomainService.createPenalty(member, reservation, reason, null);
		member.updatePenalty(true);
		penaltyRepository.save(penalty);
		PenaltyLogUtil.log("패널티 부여 완료",
			"패널티 ID: " + penalty.getId(),
			"학번: " + member.getStudentNum(),
			"사유: " + reason.name());
	}

	public void adminAssignPenalty(Member member, LocalDateTime penaltyEndAt) {
		PenaltyLogUtil.log("관리자 패널티 수동 부여 요청", "학번: " + member.getStudentNum(), "종료일자: " + penaltyEndAt);
		Penalty penalty = penaltyDomainService.createPenalty(member, null, PenaltyReasonType.ADMIN, penaltyEndAt);
		member.updatePenalty(true);
		penaltyRepository.save(penalty);
		PenaltyLogUtil.log("패널티 부여 완료",
			"패널티 ID: " + penalty.getId(),
			"학번: " + member.getStudentNum(),
			"사유: " + PenaltyReasonType.ADMIN);
	}

	public void adminDeletePenalty(Member member) {
		PenaltyLogUtil.log("관리자 패널티 삭제 요청", "학번: " + member.getStudentNum());
		Penalty penalty = penaltyDomainService.findPenaltyByMemberIdAndStatus(member);
		penalty.expirePenalty();
		member.updatePenalty(false);
		PenaltyLogUtil.log("관리자 패널티 삭제 완료", "학번: " + member.getStudentNum(), "패널티 ID: " + penalty.getId());
	}
}
