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
	private final MemberDomainService memberDomainService;

	@Transactional
	@Scheduled(cron = "0 0 0 * * *") // 매일 00:00에 실행
	public void updateMemberPenalty() {
		log.info("Processing update member penalty at {} ", LocalDateTime.now());

		penaltyRepository.findByStatus(PenaltyStatus.VALID).forEach(penalty -> {
			if(penalty.isExpired()){
				Member member = penalty.getMember();
				member.updatePenalty(false);

				log.info("해당 유저의 패널티가 해제되었습니다.: {} (ID: {}) at {}", member.getName(),
					member.getStudentNum(), LocalDateTime.now());
			}
		});

		log.info("Member Penalty updated successfully at {}", LocalDateTime.now());
	}

	//@Scheduled(cron = "0 1 10-23 * * 1-5") // 평일 10:01 ~ 23:01
	@Transactional
	@Scheduled(cron = "0 40,42,44 10-23 * * *") // 테스트를 위한 임시 적용
	public void processNoShowPenalties() {
		log.info("Processing no-show penalties at {}", LocalDateTime.now());

		LocalDateTime now = LocalDateTime.now();
		LocalDate todayDate = now.toLocalDate();  //오늘 날짜
		LocalTime todayTime = now.toLocalTime(); //현재 시간

		log.info("Processing no-show penalties for date: {} and time: {}", todayDate, todayTime);

		List<Reservation> expiredReservations = reservationRepository
			.findByScheduleDateAndEndTime(todayDate, todayTime.minusMinutes(1));

		expiredReservations.forEach(reservation -> {
			if (reservation.getStatus() == ReservationStatus.RESERVED
				&& reservation.checkAttendanceStatus(now) == ReservationStatus.NO_SHOW) {

				Member member = memberDomainService.getMemberByEmail(reservation.getUserEmail());
				penaltyService.assignPenalty(member, reservation.getId(), PenaltyReasonType.NO_SHOW);
				log.info("해당 유저가 노쇼로 인해 7일 패널티가 부여되었습니다. 이름 : {} 학번 : {}", member.getName(), member.getStudentNum());
			}
			log.info("Processed no-show for reservation: {}", reservation.getId());
		});
	}
}
