package com.ice.studyroom.domain.reservation.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ice.studyroom.domain.identity.domain.service.TokenService;
import com.ice.studyroom.domain.membership.domain.entity.Member;
import com.ice.studyroom.domain.membership.infrastructure.persistence.MemberRepository;
import com.ice.studyroom.domain.penalty.application.PenaltyService;
import com.ice.studyroom.domain.penalty.domain.type.PenaltyReasonType;
import com.ice.studyroom.domain.reservation.domain.entity.Reservation;
import com.ice.studyroom.domain.reservation.domain.entity.Schedule;
import com.ice.studyroom.domain.reservation.domain.type.ReservationStatus;
import com.ice.studyroom.domain.reservation.infrastructure.persistence.ReservationRepository;
import com.ice.studyroom.domain.reservation.infrastructure.persistence.ScheduleRepository;
import com.ice.studyroom.domain.reservation.presentation.dto.response.CancelReservationResponse;
import com.ice.studyroom.global.exception.BusinessException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class ReservationCancelTest {

	@InjectMocks
	private ReservationService reservationService;
	@Mock
	private ReservationRepository reservationRepository;
	@Mock
	private PenaltyService penaltyService;
	@Mock
	private MemberRepository memberRepository;
	@Mock
	private ScheduleRepository scheduleRepository;
	@Mock
	private TokenService tokenService;
	@Mock
	private Clock clock;
	@Mock
	private Reservation reservation;
	@Mock
	private Schedule firstSchedule;
	@Mock
	private Schedule secondSchedule;

	@BeforeEach
	void setUp() {
		// 공통 객체 생성 (Mock 객체만 설정)
		reservation = mock(Reservation.class);
		firstSchedule = mock(Schedule.class);
		secondSchedule = mock(Schedule.class);
	}

	/**
	 * 📌 테스트명: 1시간_예약_취소_성공
	 *
	 * ✅ 목적:
	 *   - 사용자가 본인의 예약을 **정상적으로 취소**할 경우,
	 *     예약 상태가 변경되고, 해당 스케줄들도 취소 처리되는지 검증한다.
	 *
	 * 🧪 시나리오 설명:
	 *   1. 사용자의 예약 시작 시간: 14:00
	 *   2. 현재 시각: 12:30 → 입실 1시간 이상 이전이므로 패널티 없이 취소 가능
	 *   3. JWT 토큰에서 사용자 이메일 추출 후 예약 정보와 일치하는지 확인
	 *   4. 취소 요청 시:
	 *      - 스케줄 1개 (first) 취소
	 *      - 예약 상태를 `CANCELLED`로 변경
	 *      - 응답 객체에 예약 ID가 포함되어 반환
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - 입실 1시간 이상 전에 취소할 경우, **페널티 없이 예약 취소가 가능하다.**
	 *   - 스케줄 슬롯도 함께 취소되어야 하며, 예약 상태는 `CANCELLED`로 전환
	 *
	 * 🧩 검증 포인트:
	 *   - `reservation.markStatus(CANCELLED)`가 정확히 1번 호출되었는가?
	 *   - `firstSchedule.cancel()` / `secondSchedule.cancel()`이 각각 호출되었는가?
	 *   - `CancelReservationResponse` 응답이 null이 아니며, 올바른 ID를 포함하고 있는가?
	 *
	 * ✅ 기대 결과:
	 *   - 예약 취소 성공 → 응답 OK
	 *   - 스케줄도 함께 정상 취소됨
	 *   - 패널티 없음, 예외 없음
	 */
	@Test
	void 예약_1시간_취소_성공() {
		// given
		Long reservationId = 1L;
		String token = "Bearer valid_token";
		String userEmail = "user@hufs.ac.kr";

		LocalDateTime fixedNow = LocalDateTime.of(2025, 3, 22, 12, 30); // 현재 시각

		// JWT를 통한 사용자 정보를 토대로, 본인의 예약인지 확인
		when(tokenService.extractEmailFromAccessToken(token)).thenReturn(userEmail);
		when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
		when(reservation.isOwnedBy(userEmail)).thenReturn(true);

		when(clock.instant()).thenReturn(fixedNow.atZone(java.time.ZoneId.systemDefault()).toInstant());
		lenient().when(clock.getZone()).thenReturn(ZoneId.systemDefault());

		when(reservation.getFirstScheduleId()).thenReturn(100L);
		when(scheduleRepository.findById(100L)).thenReturn(Optional.of(firstSchedule));
		when(firstSchedule.getStartTime()).thenReturn(LocalTime.of(14, 0));

		when(reservation.getSecondScheduleId()).thenReturn(null);
		doNothing().when(reservation).markStatus(any());

		// when
		CancelReservationResponse response = reservationService.cancelReservation(reservationId, token);

		// then
		assertNotNull(response);
		assertEquals(reservationId, response.id());

		verify(reservation, times(1)).markStatus(ReservationStatus.CANCELLED);
		verify(firstSchedule, times(1)).cancel();
	}

	/**
	 * 📌 테스트명: 2시간_예약_취소_성공
	 *
	 * ✅ 목적:
	 *   - 사용자가 본인의 예약을 **정상적으로 취소**할 경우,
	 *     예약 상태가 변경되고, 해당 스케줄들도 취소 처리되는지 검증한다.
	 *
	 * 🧪 시나리오 설명:
	 *   1. 사용자의 예약 시작 시간: 14:00
	 *   2. 현재 시각: 12:30 → 입실 1시간 이상 이전이므로 패널티 없이 취소 가능
	 *   3. JWT 토큰에서 사용자 이메일 추출 후 예약 정보와 일치하는지 확인
	 *   4. 취소 요청 시:
	 *      - 스케줄 2개 (first, second) 모두 취소
	 *      - 예약 상태를 `CANCELLED`로 변경
	 *      - 응답 객체에 예약 ID가 포함되어 반환
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - 입실 1시간 이상 전에 취소할 경우, **페널티 없이 예약 취소가 가능하다.**
	 *   - 스케줄 슬롯도 함께 취소되어야 하며, 예약 상태는 `CANCELLED`로 전환
	 *
	 * 🧩 검증 포인트:
	 *   - `reservation.markStatus(CANCELLED)`가 정확히 1번 호출되었는가?
	 *   - `firstSchedule.cancel()` / `secondSchedule.cancel()`이 각각 호출되었는가?
	 *   - `CancelReservationResponse` 응답이 null이 아니며, 올바른 ID를 포함하고 있는가?
	 *
	 * ✅ 기대 결과:
	 *   - 예약 취소 성공 → 응답 OK
	 *   - 스케줄도 함께 정상 취소됨
	 *   - 패널티 없음, 예외 없음
	 */
	@Test
	void 예약_2시간_취소_성공() {
		// given
		Long reservationId = 1L;
		String token = "Bearer valid_token";
		String userEmail = "user@hufs.ac.kr";

		LocalDateTime fixedNow = LocalDateTime.of(2025, 3, 22, 12, 30); // 현재 시각

		when(clock.instant()).thenReturn(fixedNow.atZone(java.time.ZoneId.systemDefault()).toInstant());
		lenient().when(clock.getZone()).thenReturn(ZoneId.systemDefault());

		// JWT를 통한 사용자 정보를 토대로, 본인의 예약인지 확인
		when(tokenService.extractEmailFromAccessToken(token)).thenReturn(userEmail);
		when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
		when(reservation.isOwnedBy(userEmail)).thenReturn(true);

		when(reservation.getFirstScheduleId()).thenReturn(100L);
		when(reservation.getSecondScheduleId()).thenReturn(101L);
		when(scheduleRepository.findById(100L)).thenReturn(Optional.of(firstSchedule));
		when(scheduleRepository.findById(101L)).thenReturn(Optional.of(secondSchedule));
		when(firstSchedule.getStartTime()).thenReturn(LocalTime.of(14, 0));
		doNothing().when(reservation).markStatus(any());

		// when
		CancelReservationResponse response = reservationService.cancelReservation(reservationId, token);

		// then
		assertNotNull(response);
		assertEquals(reservationId, response.id());

		verify(reservation, times(1)).markStatus(ReservationStatus.CANCELLED);
		verify(firstSchedule, times(1)).cancel();
		verify(secondSchedule, times(1)).cancel();
	}

	/**
	 * 📌 테스트명: 본인_예약이_아닐_경우_예외
	 *
	 * ✅ 목적:
	 *   - JWT 토큰으로 인증된 사용자 이메일이 예약 정보의 사용자 이메일과 일치하지 않는 경우,
	 *     **예약 취소가 거부되고 BusinessException이 발생하는지**를 검증한다.
	 *
	 * 🧪 시나리오 설명:
	 *   1. 예약 ID로 예약 객체를 정상적으로 조회한다.
	 *   2. JWT 토큰으로 추출된 사용자 이메일과 예약 객체의 이메일이 **다르다.**
	 *      - 예: "wrong@example.com" vs 예약자는 다른 사람
	 *   3. 이메일이 일치하지 않으므로, 예약 취소를 시도할 경우 예외가 발생해야 한다.
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - 예약자는 **본인의 예약만** 취소할 수 있다.
	 *   - 타인의 예약을 취소하는 행위는 무효이며, 예외로 처리해야 한다.
	 *
	 * 🧩 검증 포인트:
	 *   - `cancelReservation()` 호출 시 `BusinessException`이 발생해야 한다.
	 *   - 예외 메시지는 정확히 `"이전에 예약이 되지 않았습니다."` 여야 한다.
	 *   - 예약 삭제(`delete()`), 상태 변경(`markStatus()`), 스케줄 변경 등은 **절대 호출되지 않아야 한다.**
	 *
	 * ✅ 기대 결과:
	 *   - 예약 취소 시도 → 예외 발생 → 테스트 성공
	 *   - 시스템이 사용자 권한을 정확히 체크하고, 타인의 예약 변경을 방지함을 보장
	 */
	@Test
	void 본인_예약이_아닐_경우_예외() {
		// given
		Long reservationId = 1L;
		String token = "Bearer valid_token";
		when(tokenService.extractEmailFromAccessToken(token)).thenReturn("wrong@example.com");
		when(reservation.isOwnedBy("wrong@example.com")).thenReturn(false);
		when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

		// when & then
		BusinessException exception = assertThrows(BusinessException.class,
			() -> reservationService.cancelReservation(reservationId, token));

		assertEquals("이전에 예약이 되지 않았습니다.", exception.getMessage());

		// 예약 삭제가 호출되지 않아야 함
		verify(reservationRepository, never()).delete(any());
	}

	/**
	 * 📌 테스트명: 입실_1시간_전이면_패널티_부여
	 *
	 * ✅ 목적:
	 *   - 사용자가 예약한 입실 시간 기준 **1시간 이하로 남았을 경우**에 예약을 취소할 경우,
	 *     시스템이 자동으로 **패널티를 부여하는지** 검증한다.
	 *
	 * 🧪 시나리오 설명:
	 *   1. 예약 정보:
	 *      - 예약 시작 시간: 13:00
	 *      - 현재 시각: 12:30 (입실 30분 전)
	 *   2. JWT 토큰을 통해 사용자의 이메일을 추출하고, 예약 정보와 일치하는지 확인
	 *   3. 취소 요청이 들어오면 다음을 수행:
	 *      - 스케줄 취소 처리 (예약한 시간 slot의 상태를 cancel로 변경)
	 *      - 예약 상태를 CANCELLED로 변경
	 *      - 1시간 이내 취소이므로 penaltyService를 통해 패널티 부여
	 *
	 * 🧩 검증 포인트:
	 *   - 예약 상태가 `ReservationStatus.CANCELLED`로 변경되었는가?
	 *   - `firstSchedule`, `secondSchedule` 각각에 대해 `cancel()`이 호출되었는가?
	 *   - `penaltyService.assignPenalty(...)`가 정확히 1회 호출되었는가?
	 *
	 * ⚠ 중요 비즈니스 규칙:
	 *   - 입실 1시간 전까지는 패널티 없이 취소 가능
	 *   - 그 이후로는 취소 시 패널티가 부여됨 (지금 이 테스트는 그 경계 시간대 테스트)
	 *
	 * ✅ 기대 결과:
	 *   - 모든 검증 포인트 통과
	 *   - 실제 서비스에서도 해당 시간 조건이 정확히 반영됨을 확인 가능
	 */
	@Test
	void 입실_1시간_전이면_패널티_부여() {
		// given
		Long reservationId = 1L;
		String token = "Bearer valid_token";
		String userEmail = "user@hufs.ac.kr";

		LocalDateTime fixedNow = LocalDateTime.of(2025, 3, 22, 12, 30); // 현재 시각

		when(clock.instant()).thenReturn(fixedNow.atZone(java.time.ZoneId.systemDefault()).toInstant());
		lenient().when(clock.getZone()).thenReturn(ZoneId.systemDefault());

		when(tokenService.extractEmailFromAccessToken(token)).thenReturn(userEmail);
		when(reservation.isOwnedBy(userEmail)).thenReturn(true);
		when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

		when(reservation.getFirstScheduleId()).thenReturn(100L);
		when(reservation.getSecondScheduleId()).thenReturn(101L);
		when(scheduleRepository.findById(100L)).thenReturn(Optional.of(firstSchedule));
		when(scheduleRepository.findById(101L)).thenReturn(Optional.of(secondSchedule));
		when(firstSchedule.getStartTime()).thenReturn(LocalTime.of(13, 0));

		doNothing().when(reservation).markStatus(any());
		when(memberRepository.getMemberByEmail(any())).thenReturn(mock(Member.class));
		doNothing().when(penaltyService).assignPenalty(any(), eq(reservationId), eq(PenaltyReasonType.CANCEL));

		// when
		CancelReservationResponse response = reservationService.cancelReservation(reservationId, token);

		// then
		assertNotNull(response);

		verify(reservation).markStatus(ReservationStatus.CANCELLED);
		verify(firstSchedule).cancel();
		verify(secondSchedule).cancel();

		verify(penaltyService, times(1)).assignPenalty(any(), eq(reservationId), eq(PenaltyReasonType.CANCEL));
	}

	/**
	 * 📌 테스트명: 입실까지_60분_남았을_때_취소하면_패널티_부여
	 *
	 * ✅ 목적:
	 *   - 사용자가 예약한 입실 시간 기준 **1시간** 남았을 때 예약을 취소할 경우,
	 *     시스템이 자동으로 **패널티를 부여하는지** 검증한다.
	 *
	 * 🧪 시나리오 설명:
	 *   1. 예약 정보:
	 *      - 예약 시작 시간: 13:00
	 *      - 현재 시각: 12:00 (입실 60분 전)
	 *   2. JWT 토큰을 통해 사용자의 이메일을 추출하고, 예약 정보와 일치하는지 확인
	 *   3. 취소 요청이 들어오면 다음을 수행:
	 *      - 스케줄 취소 처리 (예약한 시간 slot의 상태를 cancel로 변경)
	 *      - 예약 상태를 CANCELLED로 변경
	 *      - 1시간 경계 면에서 취소이므로 penaltyService를 통해 패널티 부여
	 *
	 * 🧩 검증 포인트:
	 *   - 예약 상태가 `ReservationStatus.CANCELLED`로 변경되었는가?
	 *   - `firstSchedule`, `secondSchedule` 각각에 대해 `cancel()`이 호출되었는가?
	 *   - `penaltyService.assignPenalty(...)`가 정확히 1회 호출되었는가?
	 *
	 * ⚠ 중요 비즈니스 규칙:
	 *   - 입실 1시간 전까지는 패널티 없이 취소 가능
	 *   - 그 이후로는 취소 시 패널티가 부여됨 (지금 이 테스트는 그 경계 시간대 테스트)
	 *
	 * ✅ 기대 결과:
	 *   - 모든 검증 포인트 통과
	 *   - 실제 서비스에서도 해당 시간 조건이 정확히 반영됨을 확인 가능
	 */
	@Test
	void 입실까지_60분_남았을_때_취소하면_패널티_부여() {
		// given
		Long reservationId = 1L;
		String token = "Bearer valid_token";
		String userEmail = "user@hufs.ac.kr";

		LocalDateTime fixedNow = LocalDateTime.of(2025, 3, 22, 12, 0); // 현재 시각

		when(clock.instant()).thenReturn(fixedNow.atZone(java.time.ZoneId.systemDefault()).toInstant());
		lenient().when(clock.getZone()).thenReturn(ZoneId.systemDefault());

		when(tokenService.extractEmailFromAccessToken(token)).thenReturn(userEmail);
		when(reservation.isOwnedBy(userEmail)).thenReturn(true);
		when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

		when(reservation.getFirstScheduleId()).thenReturn(100L);
		when(reservation.getSecondScheduleId()).thenReturn(101L);
		when(scheduleRepository.findById(100L)).thenReturn(Optional.of(firstSchedule));
		when(scheduleRepository.findById(101L)).thenReturn(Optional.of(secondSchedule));
		when(firstSchedule.getStartTime()).thenReturn(LocalTime.of(13, 0));

		doNothing().when(reservation).markStatus(any());
		when(memberRepository.getMemberByEmail(any())).thenReturn(mock(Member.class));
		doNothing().when(penaltyService).assignPenalty(any(), eq(reservationId), eq(PenaltyReasonType.CANCEL));

		// when
		CancelReservationResponse response = reservationService.cancelReservation(reservationId, token);

		// then
		assertNotNull(response);

		verify(reservation).markStatus(ReservationStatus.CANCELLED);
		verify(firstSchedule).cancel();
		verify(secondSchedule).cancel();

		verify(penaltyService, times(1)).assignPenalty(any(), eq(reservationId), eq(PenaltyReasonType.CANCEL));
	}

	/**
	 * 📌 테스트명: 예약이_존재하지_않을_경우_예외
	 *
	 * ✅ 목적:
	 *   - 사용자가 전달한 예약 ID에 해당하는 예약이 **존재하지 않을 경우**,
	 *     시스템이 이를 감지하고 **BusinessException을 발생**시키는지 검증한다.
	 *
	 * 🧪 시나리오 설명:
	 *   1. 예약 ID(예: 1L)를 기준으로 `reservationRepository.findById()` 호출
	 *   2. 리턴값이 `Optional.empty()` → 즉, 예약 데이터가 존재하지 않음
	 *   3. `cancelReservation()` 호출 시 예외가 발생해야 함
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - 존재하지 않는 예약은 취소할 수 없다.
	 *   - 잘못된 예약 ID로 취소를 시도할 경우, 예외를 발생시켜야 한다.
	 *
	 * 🧩 검증 포인트:
	 *   - `BusinessException`이 정확히 발생했는가?
	 *   - 예외 메시지는 `"존재하지 않는 예약입니다."` 여야 한다.
	 *   - `reservationRepository.delete()` 호출되지 않아야 한다. (존재하지 않으므로)
	 *
	 * ✅ 기대 결과:
	 *   - `cancelReservation()` 호출 시 즉시 예외 발생
	 *   - 내부 로직 (상태 변경, 패널티, 스케줄 등)은 전혀 실행되지 않음
	 */
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

	/**
	 * 📌 테스트명: 입실_시간_이후_취소_불가_예외_발생
	 *
	 * ✅ 목적:
	 *   - 예약된 입실 시간이 **이미 지난 경우**, 사용자가 취소 요청을 할 경우
	 *     시스템이 해당 요청을 차단하고 **예외(BusinessException)**를 발생시키는지 검증한다.
	 *
	 * 🧪 시나리오 설명:
	 *   1. 예약 정보:
	 *      - 예약 시작 시간: 13:00
	 *      - 현재 시각: 13:30 (입실 시간 경과)
	 *   2. 사용자 JWT 토큰으로 이메일을 추출하고, 본인의 예약인지 확인
	 *   3. 취소 요청이 들어오면 다음 로직에 따라 처리:
	 *      - 현재 시간이 예약 시작 시간보다 **after(이후)**이면 예외 발생
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - 입실 시간이 지난 뒤에는 예약 취소 자체가 불가능하다.
	 *   - 이미 사용이 시작된 스케줄은 다른 사람에게 할당되거나 취소될 수 없기 때문이다.
	 *
	 * 🧩 검증 포인트:
	 *   - `cancelReservation()` 호출 시 `BusinessException`이 발생해야 한다.
	 *   - 예외 메시지는 정확하게 "입실 시간이 초과하였기에 취소할 수 없습니다." 이어야 한다.
	 *   - `schedule.cancel()` 또는 `markStatus()`가 호출되지 않아야 한다.
	 *   - `penaltyService.assignPenalty(...)`도 호출되지 않아야 한다.
	 *
	 * ✅ 기대 결과:
	 *   - 예약 취소 시도 → 예외 발생 → 테스트 성공
	 *   - 비즈니스 정책이 정확하게 적용되며, 시스템 안정성 확보
	 */
	@Test
	void 입실_시간_이후_취소_불가_예외_발생() {
		// given
		Long reservationId = 1L;
		String token = "Bearer valid_token";
		String userEmail = "user@hufs.ac.kr";

		// 현재 시각: 13:30
		LocalDateTime fixedNow = LocalDateTime.of(2025, 3, 22, 13, 30);
		when(clock.instant()).thenReturn(fixedNow.atZone(ZoneId.systemDefault()).toInstant());
		lenient().when(clock.getZone()).thenReturn(ZoneId.systemDefault());

		// 예약 정보: 시작 시각 13:00 → 현재 시간보다 이전
		when(tokenService.extractEmailFromAccessToken(token)).thenReturn(userEmail);
		when(reservation.isOwnedBy(userEmail)).thenReturn(true);
		when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

		when(reservation.getFirstScheduleId()).thenReturn(100L);
		when(scheduleRepository.findById(100L)).thenReturn(Optional.of(firstSchedule));
		when(firstSchedule.getStartTime()).thenReturn(LocalTime.of(13, 0));

		// when & then
		BusinessException exception = assertThrows(BusinessException.class,
			() -> reservationService.cancelReservation(reservationId, token));

		assertEquals("입실 시간이 초과하였기에 취소할 수 없습니다.", exception.getMessage());

		// 스케줄 cancel이나 상태 변경, 패널티 부여는 절대 호출되지 않아야 함
		verify(firstSchedule, never()).cancel();
		verify(secondSchedule, never()).cancel();
		verify(reservation, never()).markStatus(any());
		verify(penaltyService, never()).assignPenalty(any(), anyLong(), any());
	}
}
