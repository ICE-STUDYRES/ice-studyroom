package com.ice.studyroom.domain.reservation.scheduler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ice.studyroom.domain.reservation.domain.type.ReservationStatus;
import com.ice.studyroom.domain.reservation.infrastructure.persistence.ReservationRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class ReservationUpdateScheduler {

	private final ReservationRepository reservationRepository;

	@Transactional
	@Scheduled(cron = "0 49 10-23 * * 1-5")
	public void updateCompleteReservations(){

		LocalDateTime now = LocalDateTime.now();
		LocalDate todayDate = now.toLocalDate();
		LocalTime todayTime = now.toLocalTime();

		log.info("Processing update reservation status to complete for date: {} and time: {}", todayDate, todayTime);

		reservationRepository.findByScheduleDateAndEndTime(todayDate, todayTime.minusMinutes(1)).forEach(reservation -> {
			if(reservation.getStatus() == ReservationStatus.ENTRANCE){
				reservation.markStatus(ReservationStatus.COMPLETED);
			}
			log.info("예약이 종료되었습니다.: {} (ID: {}) at {}", reservation.getUserEmail(), reservation.getId(), LocalDateTime.now());
		});
	}

}
