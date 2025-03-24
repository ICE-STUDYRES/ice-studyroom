package com.ice.studyroom.domain.reservation.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ice.studyroom.domain.identity.domain.service.TokenService;
import com.ice.studyroom.domain.membership.domain.entity.Member;
import com.ice.studyroom.domain.membership.domain.vo.Email;
import com.ice.studyroom.domain.reservation.domain.entity.Reservation;
import com.ice.studyroom.domain.reservation.domain.entity.Schedule;
import com.ice.studyroom.domain.reservation.domain.type.ReservationStatus;
import com.ice.studyroom.domain.reservation.infrastructure.persistence.ReservationRepository;
import com.ice.studyroom.domain.reservation.infrastructure.persistence.ScheduleRepository;
import com.ice.studyroom.global.exception.BusinessException;
import com.ice.studyroom.global.type.StatusCode;

@ExtendWith(MockitoExtension.class)
public class ReservationExtendTest {

	@Mock
	private ReservationRepository reservationRepository;

	@Mock
	private ScheduleRepository scheduleRepository;

	@Mock
	private TokenService tokenService;

	@Mock
	private Clock clock;

	@InjectMocks
	private ReservationService reservationService;

	@Mock
	private Reservation reservation;

	@Mock
	private Schedule schedule;

	@Mock
	private Member member;

	@BeforeEach
	void setUp() {
		// 공통 객체 생성 (Mock 객체만 설정)
		reservation = mock(Reservation.class);
		schedule = mock(Schedule.class);
		member = mock(Member.class);
	}

	@Test
	void 존재하지_않을_예약일_경우_예외() {
		// given
		Long invalidReservationId = 999L;
		String token = "Bearer token";

		given(reservationRepository.findById(invalidReservationId))
			.willReturn(Optional.empty());

		// when & then
		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.extendReservation(invalidReservationId, token)
		);

		assertEquals(StatusCode.NOT_FOUND, ex.getStatusCode());
		assertEquals("존재하지 않는 예약입니다.", ex.getMessage());
	}

	@Test
	void 예약_소유자가_아닌_경우_예외() {
		// given
		Long reservationId = 1L;
		String token = "Bearer token";
		String notOwnerEmail = "not-owner@hufs.ac.kr";

		given(reservationRepository.findById(reservationId)).willReturn(Optional.of(reservation));
		given(tokenService.extractEmailFromAccessToken(token)).willReturn(notOwnerEmail);
		given(reservation.isOwnedBy(notOwnerEmail)).willReturn(false);

		// when & then
		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.extendReservation(reservationId, token)
		);

		assertEquals("해당 예약 정보가 존재하지 않습니다.", ex.getMessage());
	}

	@Test
	void 연장_요청이_이른_경우_예외() {
		// given
		Long reservationId = 1L;
		String token = "Bearer token";
		String ownerEmail = "owner@hufs.ac.kr";

		통과된_기본_예약_검증_셋업(reservationId, token, ownerEmail);

		// 현재 시각: 13:49
		LocalDateTime fixedNow = LocalDateTime.of(2025, 3, 22, 13, 49);
		when(clock.instant()).thenReturn(fixedNow.atZone(ZoneId.systemDefault()).toInstant());
		lenient().when(clock.getZone()).thenReturn(ZoneId.systemDefault());

		// 예약 종료 시간: 14:00
		LocalDate reservationDate = LocalDate.of(2025, 3, 22);
		LocalTime reservationEndTime = LocalTime.of(14, 0);
		given(reservation.getScheduleDate()).willReturn(reservationDate);
		given(reservation.getEndTime()).willReturn(reservationEndTime);

		// when & then
		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.extendReservation(reservationId, token)
		);

		assertEquals("연장은 퇴실 시간 10분 전부터 가능합니다.", ex.getMessage());
	}

	@Test
	void 연장_요청이_늦은_경우_예외() {
		// given
		Long reservationId = 1L;
		String token = "Bearer token";
		String ownerEmail = "owner@hufs.ac.kr";

		통과된_기본_예약_검증_셋업(reservationId, token, ownerEmail);

		// 현재 시각: 14:01
		LocalDateTime fixedNow = LocalDateTime.of(2025, 3, 22, 14, 1);
		when(clock.instant()).thenReturn(fixedNow.atZone(ZoneId.systemDefault()).toInstant());
		lenient().when(clock.getZone()).thenReturn(ZoneId.systemDefault());

		// 예약 종료 시간: 14:00
		LocalDate reservationDate = LocalDate.of(2025, 3, 22);
		LocalTime reservationEndTime = LocalTime.of(14, 0);
		given(reservation.getScheduleDate()).willReturn(reservationDate);
		given(reservation.getEndTime()).willReturn(reservationEndTime);

		// when & then
		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.extendReservation(reservationId, token)
		);

		assertEquals("연장 가능한 시간이 지났습니다.", ex.getMessage());
	}

	@Test
	void 스케줄이_존재하지_않을_경우_예외() {
		// given
		Long reservationId = 1L;
		String token = "Bearer token";
		String ownerEmail = "owner@hufs.ac.kr";
		Long scheduleFirstId = 10L;

		통과된_기본_예약_검증_셋업(reservationId, token, ownerEmail);
		스케줄_연장_시간_검증_셋업();

		// 임의의 스케줄 ID 설정
		given(reservation.getFirstScheduleId()).willReturn(scheduleFirstId);
		given(reservation.getSecondScheduleId()).willReturn(null);
		// 다음 스케줄이 존재하지 않을 경우
		given(scheduleRepository.findById(scheduleFirstId + 1)).willReturn(Optional.empty());

		// when & then
		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.extendReservation(reservationId, token)
		);

		assertEquals("스터디룸 이용 가능 시간을 확인해주세요.", ex.getMessage());
	}

	@Test
	void 다음_스케줄의_방번호가_다를_경우_예외() {
		Long reservationId = 1L;
		String token = "Bearer token";
		String ownerEmail = "owner@hufs.ac.kr";
		Long scheduleFirstId = 10L;

		통과된_기본_예약_검증_셋업(reservationId, token, ownerEmail);
		스케줄_연장_시간_검증_셋업();

		// 임의의 스케줄 ID 설정
		given(reservation.getFirstScheduleId()).willReturn(scheduleFirstId);
		given(reservation.getSecondScheduleId()).willReturn(null);

		// 다음 스케줄이 존재하지만, 다른 방의 스케줄일 경우
		given(scheduleRepository.findById(scheduleFirstId + 1)).willReturn(Optional.of(schedule));
		given(schedule.getRoomNumber()).willReturn("409-1");
		given(reservation.getRoomNumber()).willReturn("409-2");

		// when & then
		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.extendReservation(reservationId, token)
		);

		assertEquals("스터디룸 이용 가능 시간을 확인해주세요.", ex.getMessage());

	}

	private void 통과된_기본_예약_검증_셋업(Long reservationId, String token, String email) {
		given(reservationRepository.findById(reservationId)).willReturn(Optional.of(reservation));
		given(tokenService.extractEmailFromAccessToken(token)).willReturn(email);
		given(reservation.isOwnedBy(email)).willReturn(true);
	}

	private void 스케줄_연장_시간_검증_셋업() {
		// 현재 시각: 13:59
		LocalDateTime fixedNow = LocalDateTime.of(2025, 3, 22, 13, 59);
		when(clock.instant()).thenReturn(fixedNow.atZone(ZoneId.systemDefault()).toInstant());
		lenient().when(clock.getZone()).thenReturn(ZoneId.systemDefault());

		// 예약 종료 시간: 14:00
		LocalDate reservationDate = LocalDate.of(2025, 3, 22);
		LocalTime reservationEndTime = LocalTime.of(14, 0);
		given(reservation.getScheduleDate()).willReturn(reservationDate);
		given(reservation.getEndTime()).willReturn(reservationEndTime);
	}

}
