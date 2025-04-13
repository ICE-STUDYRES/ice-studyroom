package com.ice.studyroom.domain.penalty.domain.service;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.stereotype.Service;

import com.ice.studyroom.domain.membership.domain.entity.Member;
import com.ice.studyroom.domain.penalty.domain.entity.Penalty;
import com.ice.studyroom.domain.penalty.domain.type.PenaltyReasonType;
import com.ice.studyroom.domain.penalty.domain.type.PenaltyStatus;
import com.ice.studyroom.domain.penalty.infrastructure.persistence.PenaltyRepository;
import com.ice.studyroom.domain.reservation.domain.entity.Reservation;
import com.ice.studyroom.global.exception.BusinessException;
import com.ice.studyroom.global.type.StatusCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PenaltyDomainService {

	private final Clock clock;

	private final PenaltyRepository penaltyRepository;

	public Penalty createPenalty(Member member, Reservation reservation, PenaltyReasonType reason,
		LocalDateTime penaltyEndAt) {
		LocalDateTime end = (reason == PenaltyReasonType.ADMIN)? penaltyEndAt : calculatePenaltyEnd(reason.getDurationDays());

		return Penalty.builder()
			.member(member)
			.reservation(reservation)
			.reason(reason)
			.penaltyEnd(end)
			.build();
	}

	public Penalty findPenaltyByMemberIdAndStatus(Member member){
		return penaltyRepository.findByMemberIdAndStatus(member.getId(), PenaltyStatus.VALID).orElseThrow(
			() -> new BusinessException(StatusCode.NOT_FOUND, "유효하지 않은 패널티입니다.")
		);
	}

	private LocalDateTime calculatePenaltyEnd(int durationDays) {
		LocalDate penaltyEndDate = LocalDate.now(clock);
		int addedDays = 0;

		while (addedDays < durationDays) {
			penaltyEndDate = penaltyEndDate.plusDays(1);
			if (!(penaltyEndDate.getDayOfWeek() == DayOfWeek.SATURDAY || penaltyEndDate.getDayOfWeek() == DayOfWeek.SUNDAY)) {
				addedDays++;
			}
		}

		return LocalDateTime.of(penaltyEndDate, LocalTime.of(23, 59, 59));
	}
}
