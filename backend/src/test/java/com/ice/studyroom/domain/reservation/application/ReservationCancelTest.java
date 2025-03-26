package com.ice.studyroom.domain.reservation.application;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

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
import org.junit.jupiter.api.DisplayName;
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

	Long reservationId;
	String token;
	String userEmail;

	@BeforeEach
	void setUp() {
		reservationId = 1L;
		token = "Bearer valid_token";
		userEmail = "user@hufs.ac.kr";

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
	@DisplayName("입실 1시간 취소 성공")
	void 예약_1시간_취소_성공() {
		// given
		기본_예약_정보_셋업(token, reservationId, userEmail);
		시간_고정_셋업(12, 30);
		스케줄_정보_셋업(100L, LocalTime.of(14, 0), null, false);
		willDoNothing().given(reservation).markStatus(any());

		// when
		CancelReservationResponse response = reservationService.cancelReservation(reservationId, token);

		// then
		assertNotNull(response);
		assertEquals(reservationId, response.id());

		verify(reservation).markStatus(ReservationStatus.CANCELLED);
		verify(firstSchedule).cancel();
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
	@DisplayName("예약 2시간 취소 성공")
	void 예약_2시간_취소_성공() {
		기본_예약_정보_셋업(token, reservationId, userEmail);
		시간_고정_셋업(12, 30);
		스케줄_정보_셋업(100L, LocalTime.of(14, 0), 101L, true);
		willDoNothing().given(reservation).markStatus(any());

		CancelReservationResponse response = reservationService.cancelReservation(reservationId, token);

		assertThat(response).isNotNull();
		assertThat(response.id()).isEqualTo(reservationId);

		verify(reservation).markStatus(ReservationStatus.CANCELLED);
		verify(firstSchedule).cancel();
		verify(secondSchedule).cancel();
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
	@DisplayName("본인 예약이 아닐 경우 예외")
	void 본인_예약이_아닐_경우_예외() {
		given(tokenService.extractEmailFromAccessToken(token)).willReturn("wrong@example.com");
		given(reservationRepository.findById(reservationId)).willReturn(Optional.of(reservation));
		given(reservation.isOwnedBy("wrong@example.com")).willReturn(false);

		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.cancelReservation(reservationId, token)
		);

		assertThat(ex.getMessage()).isEqualTo("이전에 예약이 되지 않았습니다.");
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
	@DisplayName("입실까지 1시간보다 적게 남았으면 패널티 부여")
	void 입실까지_1시간보다_적게_남았으면_패널티_부여() {
		기본_예약_정보_셋업(token, reservationId, userEmail);
		시간_고정_셋업(12, 30);
		스케줄_정보_셋업(100L, LocalTime.of(13, 0), 101L, true);

		willDoNothing().given(penaltyService).assignPenalty(any(), eq(reservationId), eq(PenaltyReasonType.CANCEL));
		willDoNothing().given(reservation).markStatus(any());

		CancelReservationResponse response = reservationService.cancelReservation(reservationId, token);

		assertThat(response).isNotNull();
		verify(reservation).markStatus(ReservationStatus.CANCELLED);
		verify(firstSchedule).cancel();
		verify(secondSchedule).cancel();
		verify(penaltyService).assignPenalty(any(), eq(reservationId), eq(PenaltyReasonType.CANCEL));
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
	@DisplayName("경계값 테스트 입실까지 정확히 60분 남았을 때 취소하면 패널티 부여")
	void 경계값_테스트_입실까지_정확히_60분_남았을_때_취소하면_패널티_부여() {
		기본_예약_정보_셋업(token, reservationId, userEmail);
		시간_고정_셋업(12, 0);
		스케줄_정보_셋업(100L, LocalTime.of(13, 0), 101L, true);

		willDoNothing().given(penaltyService).assignPenalty(any(), eq(reservationId), eq(PenaltyReasonType.CANCEL));
		willDoNothing().given(reservation).markStatus(any());

		CancelReservationResponse response = reservationService.cancelReservation(reservationId, token);

		assertThat(response).isNotNull();
		verify(reservation).markStatus(ReservationStatus.CANCELLED);
		verify(firstSchedule).cancel();
		verify(secondSchedule).cancel();
		verify(penaltyService).assignPenalty(any(), eq(reservationId), eq(PenaltyReasonType.CANCEL));
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
	@DisplayName("예약이 존재하지 않을 경우 예외")
	void 예약이_존재하지_않을_경우_예외() {
		given(reservationRepository.findById(reservationId)).willReturn(Optional.empty());

		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.cancelReservation(reservationId, token)
		);

		assertThat(ex.getMessage()).isEqualTo("존재하지 않는 예약입니다.");
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
	@DisplayName("입실 시간 이후 취소는 불가능 하다는 예외 발생")
	void 입실_시간_이후_취소는_불가능_하다는_예외_발생() {
		시간_고정_셋업(13, 30);
		기본_예약_정보_셋업(token, reservationId, userEmail);
		스케줄_정보_셋업(100L, LocalTime.of(13, 0), null, false);

		BusinessException ex = assertThrows(BusinessException.class, () ->
			reservationService.cancelReservation(reservationId, token)
		);

		assertThat(ex.getMessage()).isEqualTo("입실 시간이 초과하였기에 취소할 수 없습니다.");
		verify(firstSchedule, never()).cancel();
		verify(reservation, never()).markStatus(any());
		verify(penaltyService, never()).assignPenalty(any(), anyLong(), any());
	}

	void 기본_예약_정보_셋업(String token, Long reservationId, String userEmail) {
		given(tokenService.extractEmailFromAccessToken(token)).willReturn(userEmail);
		given(reservationRepository.findById(reservationId)).willReturn(Optional.of(reservation));
		given(reservation.isOwnedBy(userEmail)).willReturn(true);
	}

	void 시간_고정_셋업(int hour, int minute) {
		LocalDateTime fixedNow = LocalDateTime.of(2025, 3, 22, hour, minute);
		given(clock.instant()).willReturn(fixedNow.atZone(ZoneId.systemDefault()).toInstant());
		given(clock.getZone()).willReturn(ZoneId.systemDefault());
	}

	void 스케줄_정보_셋업(Long firstId, LocalTime firstStartTime, Long secondId, boolean includeSecond) {
		given(reservation.getFirstScheduleId()).willReturn(firstId);
		given(scheduleRepository.findById(firstId)).willReturn(Optional.of(firstSchedule));
		given(firstSchedule.getStartTime()).willReturn(firstStartTime);

		if (includeSecond) {
			given(reservation.getSecondScheduleId()).willReturn(secondId);
			given(scheduleRepository.findById(secondId)).willReturn(Optional.of(secondSchedule));
		} else {
			lenient().when(reservation.getSecondScheduleId()).thenReturn((Long) null);
		}
	}
}
