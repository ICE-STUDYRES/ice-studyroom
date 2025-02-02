package com.ice.studyroom.domain.reservation.application;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ice.studyroom.domain.reservation.domain.entity.Reservation;
import com.ice.studyroom.domain.reservation.domain.type.ReservationStatus;
import com.ice.studyroom.helper.ReservationTestHelper;

class QrRecognitionServiceTest {

	private LocalDateTime now;

	@BeforeEach
	void setup() {
		now = LocalDateTime.now();
	}

	/**
	 * 정해진 입실시간: starTime
	 * 사용자의 입실시간: enterTime
	 */
	@Test
	void 입실시간_이전_출석() {
		// given: 출석 시간이 아직 도래하지 않은 경우
		LocalDateTime startTime = now;
		LocalDateTime enterTime = startTime.minusMinutes(10);
		Reservation reservation = ReservationTestHelper.createReservationWithEnterTime(startTime);
		// when
		ReservationStatus status = reservation.checkAttendanceStatus(enterTime);

		// then
		assertEquals(ReservationStatus.RESERVED, status);
		System.out.println("status = " + status);
	}

	/**
	 * 정해진 입실시간: starTime
	 * 사용자의 입실시간: enterTime
	 */
	@Test
	void 입실_시간에_맞게_출석() {
		// given: 출석 시간이 아직 도래하지 않은 경우
		LocalDateTime startTime = now;
		Reservation reservation = ReservationTestHelper.createReservationWithEnterTime(startTime);
		// when
		ReservationStatus status = reservation.checkAttendanceStatus(startTime);

		// then
		assertEquals(ReservationStatus.ENTRANCE, status);
		System.out.println("status = " + status);
	}

	/**
	 * 정해진 입실시간: starTime
	 * 사용자의 입실시간: enterTime
	 */
	@Test
	void 입실_시간보다_30분_늦게_출석() {
		// given: 출석 시간이 아직 도래하지 않은 경우
		LocalDateTime startTime = now;
		LocalDateTime enterTime = startTime.plusMinutes(30);
		Reservation reservation = ReservationTestHelper.createReservationWithEnterTime(startTime);
		// when
		ReservationStatus status = reservation.checkAttendanceStatus(enterTime);

		// then
		assertEquals(ReservationStatus.ENTRANCE, status);
		System.out.println("status = " + status);
	}

	/**
	 * 정해진 입실시간: starTime
	 * 사용자의 입실시간: enterTime
	 */
	@Test
	void 입실시간_30분_초과_지각() {
		// given: 출석 시간이 아직 도래하지 않은 경우
		LocalDateTime startTime = now;
		LocalDateTime enterTime = startTime.plusMinutes(31);
		Reservation reservation = ReservationTestHelper.createReservationWithEnterTime(startTime);
		// when
		ReservationStatus status = reservation.checkAttendanceStatus(enterTime);

		// then
		assertEquals(ReservationStatus.LATE, status);
		System.out.println("status = " + status);
	}

	/**
	 * 정해진 입실시간: starTime
	 * 사용자의 입실시간: enterTime
	 */
	@Test
	void 입실시간_하루_이후_출석() {
		// given: 출석 시간이 아직 도래하지 않은 경우
		LocalDateTime startTime = now;
		LocalDateTime enterTime = startTime.minusDays(1);
		Reservation reservation = ReservationTestHelper.createReservationWithEnterTime(startTime);
		// when
		ReservationStatus status = reservation.checkAttendanceStatus(enterTime);

		// then
		assertEquals(ReservationStatus.RESERVED, status);
		System.out.println("status = " + status);
	}
//
// 	@Test
// 	void getMyMostRecentReservation() {
// 	}
//
// 	@Test
// 	void getMyReservationQrCode() {
// 	}
//
// 	@Test
// 	void getSchedule() {
// 	}
//
// 	@Test
// 	void qrEntrance() {
// 	}
//
// 	@Test
// 	void createGroupReservation() {
// 	}
//
// 	@Test
// 	void cancelReservation() {
// 	}
}
