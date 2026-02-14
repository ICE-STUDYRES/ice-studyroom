package com.ice.studyroom.domain.ranking.domain.service;

import com.ice.studyroom.domain.membership.domain.entity.Member;
import com.ice.studyroom.domain.membership.domain.vo.Email;
import com.ice.studyroom.domain.reservation.domain.entity.Reservation;
import com.ice.studyroom.domain.reservation.domain.type.ReservationStatus;
import com.ice.studyroom.domain.schedule.domain.entity.Schedule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class HourlyRankingScoreCalculatorTest {

	private HourlyRankingScoreCalculator calculator;

	@BeforeEach
	void setUp() {
		calculator = new HourlyRankingScoreCalculator();
	}

	private Reservation createReservation(int startHour, int endHour) {
		Member member = Member.builder()
			.id(1L)
			.email(Email.of("test@hufs.ac.kr"))
			.name("테스트")
			.studentNum("202204353")
			.build();

		return Reservation.builder()
			.member(member)
			.scheduleDate(LocalDate.now())
			.roomNumber("A101")
			.startTime(LocalTime.of(startHour, 0))
			.endTime(LocalTime.of(endHour, 0))
			.status(ReservationStatus.RESERVED)
			.isHolder(true)
			.build();
	}

	private Schedule createSchedule(int startHour, int endHour) {
		return Schedule.builder()
			.id(1L)
			.scheduleDate(LocalDate.now())
			.roomNumber("A101")
			.startTime(LocalTime.of(startHour, 0))
			.endTime(LocalTime.of(endHour, 0))
			.capacity(4)
			.minRes(1)
			.build();
	}

	@Test
	@DisplayName("1시간 예약 + ENTRANCE → 10점")
	void calculate_oneHourEntrance() {
		Reservation reservation = createReservation(10, 11);

		int score = calculator.calculate(reservation, ReservationStatus.ENTRANCE);

		assertThat(score).isEqualTo(10);
	}

	@Test
	@DisplayName("2시간 예약 + ENTRANCE → 20점")
	void calculate_twoHourEntrance() {
		Reservation reservation = createReservation(10, 12);

		int score = calculator.calculate(reservation, ReservationStatus.ENTRANCE);

		assertThat(score).isEqualTo(20);
	}

	@Test
	@DisplayName("1시간 예약 + LATE → 10점")
	void calculate_lateEntrance() {
		Reservation reservation = createReservation(10, 11);

		int score = calculator.calculate(reservation, ReservationStatus.LATE);

		assertThat(score).isEqualTo(10);
	}

	@Test
	@DisplayName("NO_SHOW → 0점")
	void calculate_noShow() {
		Reservation reservation = createReservation(10, 11);

		int score = calculator.calculate(reservation, ReservationStatus.NO_SHOW);

		assertThat(score).isZero();
	}

	@Test
	@DisplayName("CANCELLED → 0점")
	void calculate_cancelled() {
		Reservation reservation = createReservation(10, 11);

		int score = calculator.calculate(reservation, ReservationStatus.CANCELLED);

		assertThat(score).isZero();
	}

	@Test
	@DisplayName("연장 1시간 → 10점")
	void calculate_extension_oneHour() {
		Schedule schedule = createSchedule(11, 12);

		int score = calculator.calculateForSchedule(schedule);

		assertThat(score).isEqualTo(10);
	}

	@Test
	@DisplayName("2시간 예약 후 1시간 연장 → 각각 20점, 10점 반환")
	void calculate_reservation_then_extension() {
		Reservation reservation = createReservation(10, 12); // 2시간
		Schedule extension = createSchedule(12, 13);         // 1시간

		int baseScore = calculator.calculate(reservation, ReservationStatus.ENTRANCE);
		int extensionScore = calculator.calculateForSchedule(extension);

		assertThat(baseScore).isEqualTo(20);
		assertThat(extensionScore).isEqualTo(10);
		assertThat(baseScore + extensionScore).isEqualTo(30);
	}

}
