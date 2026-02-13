package com.ice.studyroom.domain.ranking.application.checkin;

import com.ice.studyroom.domain.membership.domain.entity.Member;
import com.ice.studyroom.domain.membership.domain.vo.Email;
import com.ice.studyroom.domain.ranking.application.event.RankingEventTriggerService;
import com.ice.studyroom.domain.ranking.domain.service.RankingScoreCalculator;
import com.ice.studyroom.domain.ranking.domain.service.RankingStore;
import com.ice.studyroom.domain.ranking.domain.type.RankingPeriod;
import com.ice.studyroom.domain.reservation.domain.entity.Reservation;
import com.ice.studyroom.domain.reservation.domain.type.ReservationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RankingCheckInApplicationServiceTest {

	private RankingScoreCalculator rankingScoreCalculator;
	private RankingStore rankingStore;
	private RankingEventTriggerService rankingEventTriggerService;

	private RankingCheckInApplicationService service;

	@BeforeEach
	void setUp() {
		rankingScoreCalculator = mock(RankingScoreCalculator.class);
		rankingStore = mock(RankingStore.class);
		rankingEventTriggerService = mock(RankingEventTriggerService.class);

		service = new RankingCheckInApplicationService(
			rankingScoreCalculator,
			rankingStore,
			rankingEventTriggerService
		);
	}

	private Reservation createReservation() {
		Member member = Member.builder()
			.id(1L)
			.email(Email.of("test@hufs.ac.kr"))
			.name("테스트")
			.studentNum("20201234")
			.build();

		return Reservation.builder()
			.member(member)
			.scheduleDate(LocalDate.now())
			.roomNumber("A101")
			.startTime(LocalTime.of(10, 0))
			.endTime(LocalTime.of(12, 0))
			.status(ReservationStatus.RESERVED)
			.isHolder(true)
			.build();
	}

	@Test
	@DisplayName("점수 0이면 아무 동작도 하지 않음")
	void handleCheckIn_scoreZero() {
		Reservation reservation = createReservation();

		when(rankingScoreCalculator.calculate(any(), any()))
			.thenReturn(0);

		service.handleCheckIn(reservation, ReservationStatus.NO_SHOW);

		verify(rankingStore, never()).increaseScore(any(), any(), anyInt());
		verify(rankingEventTriggerService, never()).trigger(any());
	}

	@Test
	@DisplayName("점수 > 0이면 모든 기간에 점수 반영")
	void handleCheckIn_scoreAppliedToAllPeriods() {
		Reservation reservation = createReservation();

		when(rankingScoreCalculator.calculate(any(), any()))
			.thenReturn(20);

		when(rankingStore.getRank(RankingPeriod.WEEKLY, 1L))
			.thenReturn(6)   // previous
			.thenReturn(5);  // current

		when(rankingStore.getUpperScore(any(), any()))
			.thenReturn(100);

		when(rankingStore.getScore(any(), any()))
			.thenReturn(90);

		service.handleCheckIn(reservation, ReservationStatus.ENTRANCE);

		// RankingPeriod.values() 만큼 호출됐는지 확인
		verify(rankingStore, times(RankingPeriod.values().length))
			.increaseScore(any(), eq(1L), eq(20));

		verify(rankingEventTriggerService, times(1))
			.trigger(any());
	}

	@Test
	@DisplayName("previousRank null이면 이벤트 발생 안함")
	void handleCheckIn_previousRankNull() {
		Reservation reservation = createReservation();

		when(rankingScoreCalculator.calculate(any(), any()))
			.thenReturn(20);

		when(rankingStore.getRank(RankingPeriod.WEEKLY, 1L))
			.thenReturn(null);

		service.handleCheckIn(reservation, ReservationStatus.ENTRANCE);

		verify(rankingEventTriggerService, never()).trigger(any());
	}

	@Test
	@DisplayName("gap 계산 정상 동작")
	void handleCheckIn_gapCalculation() {
		Reservation reservation = createReservation();

		when(rankingScoreCalculator.calculate(any(), any()))
			.thenReturn(20);

		when(rankingStore.getRank(RankingPeriod.WEEKLY, 1L))
			.thenReturn(6)
			.thenReturn(5);

		when(rankingStore.getUpperScore(any(), any()))
			.thenReturn(120);

		when(rankingStore.getScore(any(), any()))
			.thenReturn(100);

		service.handleCheckIn(reservation, ReservationStatus.ENTRANCE);

		ArgumentCaptor<com.ice.studyroom.domain.ranking.application.event.RankingContext> captor =
			ArgumentCaptor.forClass(com.ice.studyroom.domain.ranking.application.event.RankingContext.class);

		verify(rankingEventTriggerService).trigger(captor.capture());

		assertThat(captor.getValue().gapWithUpper())
			.isEqualTo(20);
	}
}
