package com.ice.studyroom.domain.penalty.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.stereotype.Service;

import com.ice.studyroom.domain.membership.domain.entity.Member;
import com.ice.studyroom.domain.membership.domain.service.MemberDomainService;
import com.ice.studyroom.domain.penalty.domain.entity.Penalty;
import com.ice.studyroom.domain.penalty.domain.type.PenaltyReasonType;
import com.ice.studyroom.domain.penalty.infrastructure.persistence.PenaltyRepository;
import com.ice.studyroom.domain.reservation.domain.entity.Reservation;
import com.ice.studyroom.domain.reservation.domain.type.ReservationStatus;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PenaltyService {

	private final PenaltyRepository penaltyRepository;
	private final MemberDomainService memberDomainService;

	@Transactional
	public void assignPenalty(Member member, PenaltyReasonType reason){

		Penalty penalty = Penalty.builder()
			.member(member)
			.reason(reason)
			.penaltyEnd(calculatePenaltyEnd(reason.getDurationDays()))
			.build();

		member.updatePenalty(true);
		penaltyRepository.save(penalty);
	}

	private LocalDateTime calculatePenaltyEnd(int durationDays) {
		LocalDate penaltyEndDate = LocalDate.now().plusDays(durationDays);
		return LocalDateTime.of(penaltyEndDate, LocalTime.MAX); // 23:59:59
	}

	@Transactional
	public void checkReservationNoShow(Reservation reservation, LocalDateTime now) {
		if (reservation.checkAttendanceStatus(now) == ReservationStatus.NO_SHOW) {
			Member member = memberDomainService.getMemberByEmail(reservation.getUserEmail());
			assignPenalty(member, PenaltyReasonType.NO_SHOW);
			log.info("해당 유저가 노쇼로 인해 7일 패널티가 부여되었습니다. 이름 : {} 학번 : {}", member.getName(), member.getStudentNum());
		}
		log.info("Processed no-show for reservation: {}", reservation.getId());
	}
}
