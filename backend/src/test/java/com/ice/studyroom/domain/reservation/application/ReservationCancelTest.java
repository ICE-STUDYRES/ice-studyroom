package com.ice.studyroom.domain.reservation.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ice.studyroom.domain.identity.domain.service.TokenService;
import com.ice.studyroom.domain.reservation.domain.entity.Reservation;
import com.ice.studyroom.domain.reservation.domain.entity.Schedule;
import com.ice.studyroom.domain.reservation.infrastructure.persistence.ReservationRepository;
import com.ice.studyroom.domain.reservation.infrastructure.persistence.ScheduleRepository;
import com.ice.studyroom.domain.reservation.presentation.dto.response.CancelReservationResponse;
import com.ice.studyroom.global.exception.BusinessException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class ReservationCancelTest {

	@InjectMocks
	private ReservationService reservationService;

	@Mock
	private ReservationRepository reservationRepository;

	@Mock
	private ScheduleRepository scheduleRepository;

	@Mock
	private TokenService tokenService;

	private Reservation reservation;
	private Schedule firstSchedule;
	private Schedule secondSchedule;

	@BeforeEach
	void setUp() {
		// 공통 객체 생성 (Mock 객체만 설정)
		reservation = mock(Reservation.class);
		firstSchedule = mock(Schedule.class);
		secondSchedule = mock(Schedule.class);
	}

	@Test
	void 예약_취소_성공() {
		// given
		Long reservationId = 1L;
		String token = "Bearer valid_token";
		String userEmail = "user@example.com";

		when(tokenService.extractEmailFromAccessToken(token)).thenReturn(userEmail);
		when(reservation.matchEmail(userEmail)).thenReturn(true);
		when(reservation.getFirstScheduleId()).thenReturn(100L);
		when(reservation.getSecondScheduleId()).thenReturn(101L);
		when(reservation.getStartTime()).thenReturn(LocalTime.of(14, 0));
		when(reservation.getEndTime()).thenReturn(LocalTime.of(16, 0));
		when(scheduleRepository.findById(100L)).thenReturn(Optional.of(firstSchedule));
		when(scheduleRepository.findById(101L)).thenReturn(Optional.of(secondSchedule));
		when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

		// when
		CancelReservationResponse response = reservationService.cancelReservation(reservationId, token);

		// then
		assertNotNull(response);
		assertEquals(reservationId, response.id());

		// 예약 삭제 확인
		verify(reservationRepository, times(1)).delete(reservation);
		// 스케줄 취소 확인
		verify(firstSchedule, times(1)).cancel();
		verify(secondSchedule, times(1)).cancel();
	}

	@Test
	void 본인_예약이_아닐_경우_예외() {
		// given
		Long reservationId = 1L;
		String token = "Bearer valid_token";
		when(tokenService.extractEmailFromAccessToken(token)).thenReturn("wrong@example.com");
		when(reservation.matchEmail("wrong@example.com")).thenReturn(false);
		when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

		// when & then
		BusinessException exception = assertThrows(BusinessException.class,
			() -> reservationService.cancelReservation(reservationId, token));

		assertEquals("이전에 예약이 되지 않았습니다.", exception.getMessage());

		// 예약 삭제가 호출되지 않아야 함
		verify(reservationRepository, never()).delete(any());
	}

	// @Test
	// void 입실_1시간_전이면_패널티_부여() {
	// 	// given
	// 	Long reservationId = 1L;
	// 	String token = "Bearer valid_token";
	// 	String userEmail = "user@example.com";
	//
	// 	when(tokenService.extractEmailFromAccessToken(token)).thenReturn(userEmail);
	// 	when(reservation.matchEmail(userEmail)).thenReturn(true);
	// 	when(reservation.getStartTime()).thenReturn(LocalTime.of(14, 0));
	// 	when(reservation.getEndTime()).thenReturn(LocalTime.of(16, 0));
	// 	when(reservation.getFirstScheduleId()).thenReturn(100L);
	// 	when(reservation.getSecondScheduleId()).thenReturn(101L);
	// 	when(scheduleRepository.findById(100L)).thenReturn(Optional.of(firstSchedule));
	// 	when(scheduleRepository.findById(101L)).thenReturn(Optional.of(secondSchedule));
	// 	when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
	//
	// 	try (MockedStatic<LocalDateTime> mockedTime = mockStatic(LocalDateTime.class)) {
	// 		LocalDate today = LocalDate.now();  // 현재 날짜 저장 (Mock 영향 방지)
	// 		LocalTime startTime = reservation.getStartTime();  // StartTime 저장
	//
	// 		LocalDateTime fixedTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(13, 30));
	// 		mockedTime.when(LocalDateTime::now).thenReturn(fixedTime);
	//
	// 		LocalDateTime enterTime = LocalDateTime.of(today, startTime);  // 명시적 사용
	// 		System.out.println("EnterTime = " + enterTime);  // 확인 로그 추가
	//
	//
	// 		// when
	// 		CancelReservationResponse response = reservationService.cancelReservation(reservationId, token);
	//
	// 		// then
	// 		assertNotNull(response);
	// 		verify(reservationRepository, times(1)).delete(reservation);
	// 		verify(firstSchedule, times(1)).cancel();
	// 		verify(secondSchedule, times(1)).cancel();
	// 	}
	// }

	@Test
	void 예약이_존재하지_않을_경우_예외() {
		// given
		Long reservationId = 1L;
		String token = "Bearer valid_token";

		when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

		// when & then
		BusinessException exception = assertThrows(BusinessException.class,
			() -> reservationService.cancelReservation(reservationId, token));

		assertEquals("존재하지 않는 예약입니다.", exception.getMessage());

		// 예약 삭제가 호출되지 않아야 함
		verify(reservationRepository, never()).delete(any());
	}
}
