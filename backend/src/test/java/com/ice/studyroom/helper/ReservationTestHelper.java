package com.ice.studyroom.helper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.ice.studyroom.domain.reservation.domain.entity.Reservation;
import com.ice.studyroom.domain.reservation.domain.type.ReservationStatus;

public class ReservationTestHelper {

	public static Reservation createReservationWithEnterTime(LocalDateTime startDateTime) {
		LocalTime startTime = startDateTime.toLocalTime();
		return Reservation.builder()
			.id(1L)
			.firstScheduleId(100L)
			.secondScheduleId(200L)
			.userEmail("test@example.com")
			.userName("테스트 유저")
			.scheduleDate(LocalDate.now())
			.roomNumber("A101")
			.startTime(startTime)
			.endTime(startTime.plusHours(2))
			.enterTime(null) // 출석 시간 설정 가능
			.status(ReservationStatus.RESERVED)
			.createdAt(LocalDateTime.now())
			.updatedAt(LocalDateTime.now())
			.build();
	}
}
