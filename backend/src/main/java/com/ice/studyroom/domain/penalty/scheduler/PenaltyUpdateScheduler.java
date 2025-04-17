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
import com.ice.studyroom.domain.penalty.application.PenaltyService;
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
@Component
@EnableScheduling
@RequiredArgsConstructor
public class PenaltyUpdateScheduler {

	private final PenaltyRepository penaltyRepository;
	private final ReservationRepository reservationRepository;
	private final PenaltyService penaltyService;

	@Transactional
	@Scheduled(cron = "0 0 0 * * *") // 매일 00:00에 실행
	public void updateMemberPenalty() {
		log.info("패널티 갱신 스케줄 시작 - 실행 시각: {}", LocalDateTime.now());

		penaltyRepository.findByStatus(PenaltyStatus.VALID).forEach(penalty -> {
			if(penalty.isExpired()){
				Member member = penalty.getMember();
				member.updatePenalty(false);

				log.info("패널티 해제 처리 - userId: {}, name: {}, studentNum: {}, penaltyId: {}",
					member.getId(), member.getName(), member.getStudentNum(), penalty.getId());
			}
		});

		log.info("패널티 갱신 스케줄 종료 - 완료 시각: {}", LocalDateTime.now());
	}

	@Transactional
	@Scheduled(cron = "0 1 10-23 * * 1-5") // 평일 10:01 ~ 23:01
	public void processNoShowPenalties() {

		LocalDateTime now = LocalDateTime.now();
		LocalDate todayDate = now.toLocalDate();
		LocalTime todayTime = now.toLocalTime();

		log.info("노쇼 패널티 부여 스케줄 시작 - 실행 시각: {}", LocalDateTime.now());

		List<Reservation> expiredReservations = reservationRepository
			.findByScheduleDateAndEndTime(todayDate, todayTime.minusMinutes(1));

		expiredReservations.forEach(reservation -> {
			if (reservation.getStatus() == ReservationStatus.RESERVED
				&& reservation.checkAttendanceStatus(now) == ReservationStatus.NO_SHOW) {
				reservation.markStatus(ReservationStatus.NO_SHOW);
				Member member = reservation.getMember();
				penaltyService.assignPenalty(member, reservation.getId(), PenaltyReasonType.NO_SHOW);
				log.info("노쇼 패널티 부여 - userId: {}, name: {}, studentNum: {}, reservationId: {}",
					member.getId(), member.getName(), member.getStudentNum(), reservation.getId());
			}
			log.info("노쇼 처리 완료 - reservationId: {}", reservation.getId());
		});

		log.info("노쇼 패널티 부여 스케줄 종료 - 완료 시각: {}", LocalDateTime.now());
	}
}
