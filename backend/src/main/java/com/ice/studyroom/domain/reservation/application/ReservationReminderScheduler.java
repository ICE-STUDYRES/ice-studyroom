package com.ice.studyroom.domain.reservation.application;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import com.ice.studyroom.domain.reservation.dao.ReservationRepository;
import com.ice.studyroom.global.service.EmailService;
import com.ice.studyroom.domain.reservation.domain.entity.Reservation;

@Service
@RequiredArgsConstructor
public class ReservationReminderScheduler {

	private final ReservationRepository reservationRepository;  // DB 접근
	private final EmailService emailService;  // 메일 전송 서비스

	@Scheduled(cron = "0 30,50 * * * ?")  // 매 시 30분, 50분에 실행

	public void sendReservationReminders() {
		LocalDateTime now = LocalDateTime.now();
		LocalTime currentTime = now.toLocalTime();

		System.out.println("현재 시간: " + currentTime); // 스케줄러 실행 시간 출력

		LocalTime oneHourLater = LocalTime.of(currentTime.plusHours(1).getHour(), 0, 0);//currentTime.plusHours(1); // 초 단위 포함

		// 30분에 실행: 시작 알림 확인
		if (currentTime.getMinute() == 30) {
			List<Reservation> startReservations = reservationRepository.findByScheduleDateAndStartTime(
				now.toLocalDate(),
				oneHourLater // 현재 시간 + 1시간이 startTime과 일치하는 예약 찾기
			);

			System.out.println("시작 알림 예약 리스트 크기: " + startReservations.size());

			for (Reservation reservation : startReservations) {
				if (reservation.getStartTime().equals(oneHourLater)) { // 정확히 현재 +1시간만 시작 알림
					try {
						sendReminderEmail(reservation, "시작 알림");
					} catch (Exception e) {
						System.out.println(e.getMessage());
					}
				}
			}
		}

		// 50분에 실행: 종료 알림 확인
		if (currentTime.getMinute() == 50) {
			List<Reservation> endReservations = reservationRepository.findByScheduleDateAndEndTime(
				now.toLocalDate(),
				oneHourLater // 현재 시간 + 1시간이 endTime과 일치하는 예약 찾기
			);

			System.out.println("종료 알림 예약 리스트 크기: " + endReservations.size());

			for (Reservation reservation : endReservations) {
				if (reservation.getEndTime().equals(oneHourLater)) { // 정확히 현재 +1시간만 종료 알림

					try {
						sendReminderEmail(reservation, "종료 알림");
					} catch (Exception e) {
						System.out.println(e.getMessage());
					}

				}
			}
		}
	}

	private void sendReminderEmail(Reservation reservation, String type) {
		String subject = "[스터디룸 예약 알림] " + type;
		String body;

		if (type.equals("시작 알림")) {
			body = "안녕하세요, " + reservation.getUserName() + "님!\n" +
				"스터디룸 '" + reservation.getRoomNumber() + "' 입실 30분 전입니다.\n" +
				"예약 시작 시간: " + reservation.getStartTime();
		} else if (type.equals("종료 알림")) {
			body = "안녕하세요, " + reservation.getUserName() + "님!\n" +
				"스터디룸 '" + reservation.getRoomNumber() + "' 퇴실 10분 전입니다.\n" +
				"사용하신 자리는 깨끗이 정리 후 퇴실 부탁 드립니다!\n" +
				"예약 종료 시간: " + reservation.getEndTime();
		} else {
			body = "알림 타입이 잘못되었습니다.";
		}

		emailService.sendEmail(reservation.getUserEmail(), subject, body);
	}
}






