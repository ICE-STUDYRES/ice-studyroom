package com.ice.studyroom.domain.penalty.scheduler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ice.studyroom.domain.membership.domain.entity.Member;
import com.ice.studyroom.domain.membership.domain.service.MemberDomainService;
import com.ice.studyroom.domain.membership.infrastructure.persistence.MemberRepository;
import com.ice.studyroom.domain.penalty.application.PenaltyService;
import com.ice.studyroom.domain.penalty.domain.type.PenaltyReasonType;
import com.ice.studyroom.domain.penalty.infrastructure.persistence.PenaltyRepository;
import com.ice.studyroom.domain.reservation.domain.entity.Reservation;
import com.ice.studyroom.domain.reservation.domain.type.ReservationStatus;
import com.ice.studyroom.domain.reservation.infrastructure.persistence.ReservationRepository;

import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class PenaltyUpdateScheduler {

	private final MemberRepository memberRepository;
	private final PenaltyRepository penaltyRepository;
	private final ReservationRepository reservationRepository;
	private final PenaltyService penaltyService;

	@Transactional
	@Scheduled(cron = "0 0 0 * * *") // 매일 00:00에 실행
	public void updateMemberPenalty() {
		memberRepository.findAll().forEach(member -> {
			boolean hasPenalty = penaltyRepository
				.findTopByMemberIdAndPenaltyEndAfterOrderByPenaltyEndDesc(
				member.getId(), LocalDateTime.now()).isPresent();

			member.updatePenalty(hasPenalty);
		});

		log.info("Member Penalty updated successfully at {}", LocalDateTime.now());
	}

	@Transactional
	@Scheduled(cron = "0 0 0 * * *") // 매일 00:00에 실행
	public void expireOldPenalties() {
		int updatedCount = penaltyRepository.expireOldPenalties(LocalDateTime.now());
		log.info("{} 개의 penalty 가 만료되었습니다.", updatedCount);
	}

	@Scheduled(cron = "0 1 10-23 * * 1-5") // 평일 10:01 ~ 23:01
	public void processNoShowPenalties() {
		LocalDate todayDate = LocalDate.now(); //오늘 날짜
		LocalTime todayTime = LocalTime.now().withSecond(0).withNano(0); //현재 시간
		log.info("Processing no-show penalties for date: {} and time: {}", todayDate, todayTime);

		List<Reservation> expiredReservations = reservationRepository
			.findByScheduleDateAndEndTimeBetween(todayDate, todayTime.minusMinutes(2), todayTime);

		expiredReservations.forEach(reservation -> {
			penaltyService.checkReservationNoShow(reservation, LocalDateTime.of(todayDate, todayTime));
		});
	}
}
