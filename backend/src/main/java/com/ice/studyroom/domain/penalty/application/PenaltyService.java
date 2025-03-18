package com.ice.studyroom.domain.penalty.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.stereotype.Service;

import com.ice.studyroom.domain.membership.domain.entity.Member;
import com.ice.studyroom.domain.membership.domain.service.MemberDomainService;
import com.ice.studyroom.domain.penalty.domain.entity.Penalty;
import com.ice.studyroom.domain.penalty.domain.type.PenaltyReasonType;
import com.ice.studyroom.domain.penalty.domain.type.PenaltyStatus;
import com.ice.studyroom.domain.penalty.infrastructure.persistence.PenaltyRepository;
import com.ice.studyroom.domain.reservation.domain.entity.Reservation;
import com.ice.studyroom.domain.reservation.domain.type.ReservationStatus;
import com.ice.studyroom.domain.reservation.infrastructure.persistence.ReservationRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PenaltyService {

	private final PenaltyRepository penaltyRepository;
	private final ReservationRepository reservationRepository;

	@Transactional
	public void assignPenalty(Member member, Long reservationId, PenaltyReasonType reason) {

		Penalty penalty = Penalty.builder()
			.member(member)
			.reservation(reservationRepository.findById(reservationId).get())
			.reason(reason)
			.penaltyEnd(calculatePenaltyEnd(reason.getDurationDays()))
			.build();

		member.updatePenalty(true);
		penaltyRepository.save(penalty);
	}

	@Transactional
	public void adminAssignPenalty(Member member, LocalDateTime penaltyEndAt) {

		Penalty penalty = Penalty.builder()
			.member(member)
			.reservation(null)
			.reason(PenaltyReasonType.ADMIN)
			.penaltyEnd(penaltyEndAt)
			.build();

		member.updatePenalty(true);
		penaltyRepository.save(penalty);
	}

	@Transactional
	public void adminDeletePenalty(Member member) {
		Penalty penalty = penaltyRepository.findByMemberIdAndStatus(member.getId(), PenaltyStatus.VALID).get();
		penaltyRepository.delete(penalty);
		member.updatePenalty(false);
	}

	private LocalDateTime calculatePenaltyEnd(int durationDays) {
		LocalDate penaltyEndDate = LocalDate.now().plusDays(durationDays);
		return LocalDateTime.of(penaltyEndDate, LocalTime.of(23, 59, 59)); // 23:59:59
	}
}
