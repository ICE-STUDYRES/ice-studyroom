package com.ice.studyroom.domain.reservation.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
	private Schedule firstSchedule;

	@Mock
	private Schedule secondSchedule;

	@Mock
	private Member member;

	@BeforeEach
	void setUp() {
		// 공통 객체 생성 (Mock 객체만 설정)
		reservation = mock(Reservation.class);
		firstSchedule = mock(Schedule.class);
		secondSchedule = mock(Schedule.class);
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

}
