package com.ice.studyroom.domain.reservation.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;

import com.ice.studyroom.domain.reservation.domain.service.ReservationValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ice.studyroom.global.security.service.TokenService;
import com.ice.studyroom.domain.membership.domain.entity.Member;
import com.ice.studyroom.domain.membership.domain.service.MemberDomainService;
import com.ice.studyroom.domain.membership.domain.vo.Email;
import com.ice.studyroom.domain.membership.infrastructure.persistence.MemberRepository;
import com.ice.studyroom.domain.penalty.application.PenaltyService;
import com.ice.studyroom.domain.penalty.domain.type.PenaltyReasonType;
import com.ice.studyroom.domain.reservation.domain.entity.Reservation;
import com.ice.studyroom.domain.reservation.domain.type.ReservationStatus;
import com.ice.studyroom.domain.reservation.infrastructure.persistence.ReservationRepository;
import com.ice.studyroom.domain.reservation.infrastructure.persistence.ScheduleRepository;
import com.ice.studyroom.domain.reservation.infrastructure.redis.QRCodeService;
import com.ice.studyroom.domain.reservation.infrastructure.util.QRCodeUtil;
import com.ice.studyroom.domain.reservation.presentation.dto.request.QrEntranceRequest;
import com.ice.studyroom.domain.reservation.presentation.dto.response.QrEntranceResponse;
import com.ice.studyroom.global.exception.BusinessException;
import com.ice.studyroom.global.service.EmailService;
@ExtendWith(MockitoExtension.class)
class QrEntranceTest {

	@InjectMocks
	private ReservationService reservationService;

	@Mock private QRCodeUtil qrCodeUtil;
	@Mock private TokenService tokenService;
	@Mock private MemberRepository memberRepository;
	@Mock private ReservationRepository reservationRepository;
	@Mock private ScheduleRepository scheduleRepository;
	@Mock private ReservationConcurrencyService reservationConcurrencyService;
	@Mock private ReservationValidator reservationValidator;
	@Mock private QRCodeService qrCodeService;
	@Mock private PenaltyService penaltyService;
	@Mock private MemberDomainService memberDomainService;
	@Mock private EmailService emailService;

	private final String TOKEN = "valid-token";
	private final Long RESERVATION_ID = 1L;

	private Clock clock;

	private final Member member = createMember("홍길동", "202012345", "hong@hufs.ac.kr");

	@BeforeEach
	void setup() {
		// 기본 시간: 2025-04-04 17:05
		clock = Clock.fixed(
			LocalDateTime.of(2025, 4, 4, 17, 5).atZone(ZoneId.systemDefault()).toInstant(),
			ZoneId.systemDefault()
		);
		reservationService = new ReservationService(
			qrCodeUtil, tokenService, memberRepository, reservationRepository,
			scheduleRepository, reservationConcurrencyService, reservationValidator, qrCodeService, penaltyService,
			memberDomainService, emailService, clock
		);
	}

	/**
	 * 📌 테스트명: 정상_입실_ENTRANCE
	 *
	 * ✅ 목적:
	 *   - 사용자가 예약 시간 내에 정상적으로 QR 인증을 진행했을 때, 입실 처리가 올바르게 수행되는지 검증한다.
	 *
	 * 🧪 시나리오 설명:
	 *   1. 17:00 ~ 18:00 예약을 가진 사용자 `홍길동`이 17:05에 QR 인증 요청
	 *   2. QR 토큰으로 예약 ID를 조회하고 예약 객체를 반환
	 *   3. 현재 시간이 입실 가능한 범위에 포함되므로 상태를 `ENTRANCE`로 변경
	 *   4. QR 토큰은 무효화 처리됨
	 *   5. 벌점은 부여되지 않음
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - 입실 가능 시간: 시작 시간 -5분 ~ 시작 시간 +30분 이내
	 *   - 정상 입실 시 예약 상태를 `ENTRANCE`로 변경
	 *   - 입실 성공 시 QR 토큰은 재사용 방지를 위해 무효화해야 함
	 *
	 * 🧩 검증 포인트:
	 *   - `Reservation.status`가 `ENTRANCE`로 변경되어야 한다
	 *   - `qrCodeService.invalidateToken()`이 호출되어야 한다
	 *   - `penaltyService.assignPenalty()`는 호출되지 않아야 한다
	 *   - 반환된 응답 객체의 상태값이 `ENTRANCE`여야 한다
	 *
	 * ✅ 기대 결과:
	 *   - 정상 입실 처리되고, 시스템 로그 또는 알림으로 사용자 입실 정보가 반영된다
	 *   - QR 토큰은 무효화되어 재사용이 불가능해진다
	 *   - 어떠한 패널티도 부여되지 않는다
	 */
	@Test
	void 정상_입실_ENTRANCE() {
		Reservation reservation = createReservationWith(member, LocalTime.of(17, 0), LocalTime.of(18, 0), ReservationStatus.RESERVED);

		mockQrFlow(reservation);

		QrEntranceResponse response = reservationService.qrEntrance(new QrEntranceRequest(TOKEN));

		assertThat(response.status()).isEqualTo(ReservationStatus.ENTRANCE);
		verify(qrCodeService).invalidateToken(TOKEN);
		verify(penaltyService, never()).assignPenalty(any(), any(), any());
	}

	/**
	 * 📌 테스트명: 지각_입실_LATE
	 *
	 * ✅ 목적:
	 *   - 사용자가 지각 입실 시 `LATE` 상태로 처리되고, 패널티가 부여되는지 검증한다.
	 *
	 * 🧪 시나리오 설명:
	 *   1. 17:00 ~ 18:00 예약을 가진 사용자가 17:33에 QR 인증 요청
	 *   2. 입실 가능 범위는 초과했지만 허용 최대 지각 범위 내
	 *   3. 예약 상태를 `LATE`로 변경
	 *   4. 사용자에게 지각 사유의 패널티 부여
	 *   5. QR 토큰 무효화
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - 시작 시간 +30분 이내까지 입실 가능
	 *   - +10분 초과 시 상태는 `LATE`, 패널티 사유는 `LATE`
	 *
	 * 🧩 검증 포인트:
	 *   - 응답 상태값이 `LATE`인지
	 *   - `assignPenalty()`가 호출되었는지
	 *   - `invalidateToken()` 호출 여부
	 *
	 * ✅ 기대 결과:
	 *   - 사용자의 예약 상태가 `LATE`로 변경되고 패널티가 부여됨
	 *   - 토큰은 무효화됨
	 */
	@Test
	void 지각_입실_LATE() {
		// 17:33으로 clock 설정
		setClock(LocalDateTime.of(2025, 4, 4, 17, 33));
		Reservation reservation = createReservationWith(member, LocalTime.of(17, 0), LocalTime.of(18, 0), ReservationStatus.RESERVED);

		mockQrFlow(reservation);

		QrEntranceResponse response = reservationService.qrEntrance(new QrEntranceRequest(TOKEN));

		assertThat(response.status()).isEqualTo(ReservationStatus.LATE);
		verify(penaltyService).assignPenalty(member, RESERVATION_ID, PenaltyReasonType.LATE);
		verify(qrCodeService).invalidateToken(TOKEN);
	}

	/**
	 * 📌 테스트명: 입실_시간_전_RESERVED_예외
	 *
	 * ✅ 목적:
	 *   - 예약 시작 이전에 QR 인증을 시도할 경우 예외가 발생하는지 검증한다.
	 *
	 * 🧪 시나리오 설명:
	 *   1. 17:00 ~ 18:00 예약을 가진 사용자가 16:50에 QR 인증 요청
	 *   2. 입실 가능 시간 이전이므로 예외 발생
	 *   3. QR 토큰은 무효화되지만, 패널티는 부여되지 않음
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - 입실 시도는 시작 시간 -5분부터 가능
	 *   - 이보다 빠른 시도는 예외 처리 (`출석 시간이 아닙니다`)
	 *
	 * 🧩 검증 포인트:
	 *   - 예외 메시지에 "출석 시간이 아닙니다" 포함 여부
	 *   - `invalidateToken()` 호출 여부
	 *   - `assignPenalty()`는 호출되지 않아야 함
	 *
	 * ✅ 기대 결과:
	 *   - 예외 발생 후 QR 토큰은 무효화되고, 패널티는 부여되지 않음
	 */
	@Test
	void 입실_시간_전_RESERVED_예외() {
		// 16:50으로 clock 설정
		setClock(LocalDateTime.of(2025, 4, 4, 16, 50));
		Reservation reservation = createReservationWith(member, LocalTime.of(17, 0), LocalTime.of(18, 0), ReservationStatus.RESERVED);

		mockQrFlow(reservation);

		assertThatThrownBy(() -> reservationService.qrEntrance(new QrEntranceRequest(TOKEN)))
			.isInstanceOf(BusinessException.class)
			.hasMessageContaining("출석 시간이 아닙니다");

		verify(qrCodeService).invalidateToken(TOKEN); // 이 시점에도 무효화는 수행
		verify(penaltyService, never()).assignPenalty(any(), any(), any());
	}

	/**
	 * 📌 테스트명: 노쇼_NO_SHOW_예외
	 *
	 * ✅ 목적:
	 *   - 사용자가 입실 가능한 시간을 초과했을 경우 노쇼로 간주되어 예외가 발생하는지 검증한다.
	 *
	 * 🧪 시나리오 설명:
	 *   1. 17:00 ~ 18:00 예약을 가진 사용자가 18:10에 QR 인증 시도
	 *   2. 입실 가능한 시간 범위를 초과했기 때문에 예외 발생
	 *   3. QR 토큰은 무효화되지만, 패널티는 부여되지 않음
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - 시작 시간 +30분 이후 입실 시도는 불가
	 *   - 예외 메시지는 "출석 시간이 만료되었습니다"
	 *
	 * 🧩 검증 포인트:
	 *   - 예외 메시지에 "출석 시간이 만료되었습니다" 포함 여부
	 *   - `invalidateToken()` 호출 여부
	 *   - `assignPenalty()`는 호출되지 않아야 함
	 *
	 * ✅ 기대 결과:
	 *   - 예외 발생 및 QR 토큰 무효화
	 *   - 사용자에게 패널티는 부여되지 않음 (별도 처리 가능)
	 */
	@Test
	void 노쇼_NO_SHOW_예외() {
		// 18:10으로 clock 설정
		setClock(LocalDateTime.of(2025, 4, 4, 18, 10));
		Reservation reservation = createReservationWith(member, LocalTime.of(17, 0), LocalTime.of(18, 0), ReservationStatus.RESERVED);

		mockQrFlow(reservation);

		assertThatThrownBy(() -> reservationService.qrEntrance(new QrEntranceRequest(TOKEN)))
			.isInstanceOf(BusinessException.class)
			.hasMessageContaining("출석 시간이 만료되었습니다");

		verify(qrCodeService).invalidateToken(TOKEN);
		verify(penaltyService, never()).assignPenalty(any(), any(), any());
	}

	/**
	 * 📌 테스트명: 이미_입실된_예약_예외
	 *
	 * ✅ 목적:
	 *   - 이미 입실 처리된 예약에 대해 QR 인증을 시도할 경우 예외가 발생하는지 검증한다.
	 *
	 * 🧪 시나리오 설명:
	 *   1. 예약 상태가 이미 `ENTRANCE`인 경우
	 *   2. QR 인증을 시도하면 예외 발생
	 *   3. QR 토큰 무효화 또는 패널티 처리는 수행되지 않음
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - 한 번 입실 처리된 예약은 다시 입실할 수 없음
	 *   - 예외 메시지는 "이미 입실처리 된 예약입니다"
	 *
	 * 🧩 검증 포인트:
	 *   - 예외 메시지에 "이미 입실처리 된 예약입니다" 포함 여부
	 *   - `invalidateToken()` 및 `assignPenalty()`는 호출되지 않아야 함
	 *
	 * ✅ 기대 결과:
	 *   - 예외 발생 후 시스템 상태에는 변화 없음
	 */
	@Test
	void 이미_입실된_예약_예외() {
		Reservation reservation = createReservationWith(member, LocalTime.of(17, 0), LocalTime.of(18, 0), ReservationStatus.ENTRANCE);

		mockQrFlow(reservation);

		assertThatThrownBy(() -> reservationService.qrEntrance(new QrEntranceRequest(TOKEN)))
			.isInstanceOf(BusinessException.class)
			.hasMessageContaining("이미 입실처리 된 예약입니다");

		verify(qrCodeService, never()).invalidateToken(any());
		verify(penaltyService, never()).assignPenalty(any(), any(), any());
	}

	/**
	 * 📌 테스트명: 취소된_예약_예외
	 *
	 * ✅ 목적:
	 *   - 이미 취소된 예약에 대해 QR 인증을 시도할 경우 예외가 발생하는지 검증한다.
	 *
	 * 🧪 시나리오 설명:
	 *   1. 예약 상태가 `CANCELLED`인 경우
	 *   2. 사용자가 QR 인증 시도
	 *   3. 예외 발생하며 토큰/패널티 처리 없음
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - 취소된 예약은 QR 인증으로 입실할 수 없음
	 *   - 예외 메시지는 "취소된 예약입니다"
	 *
	 * 🧩 검증 포인트:
	 *   - 예외 메시지에 "취소된 예약입니다" 포함 여부
	 *   - `invalidateToken()` 및 `assignPenalty()`는 호출되지 않아야 함
	 *
	 * ✅ 기대 결과:
	 *   - 예외 발생 후 시스템 상태에는 변화 없음
	 */
	@Test
	void 취소된_예약_예외() {
		Reservation reservation = createReservationWith(member, LocalTime.of(17, 0), LocalTime.of(18, 0), ReservationStatus.CANCELLED);

		mockQrFlow(reservation);

		assertThatThrownBy(() -> reservationService.qrEntrance(new QrEntranceRequest(TOKEN)))
			.isInstanceOf(BusinessException.class)
			.hasMessageContaining("취소된 예약입니다");

		verify(qrCodeService, never()).invalidateToken(any());
		verify(penaltyService, never()).assignPenalty(any(), any(), any());
	}

	// ========== 헬퍼 메서드 ==========

	private void mockQrFlow(Reservation reservation) {
		given(qrCodeService.getReservationIdByToken(TOKEN)).willReturn(RESERVATION_ID);
		given(reservationRepository.findById(RESERVATION_ID)).willReturn(Optional.of(reservation));
	}

	private void setClock(LocalDateTime dateTime) {
		this.clock = Clock.fixed(dateTime.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
		reservationService = new ReservationService(
			qrCodeUtil, tokenService, memberRepository, reservationRepository,
			scheduleRepository, reservationConcurrencyService, reservationValidator, qrCodeService, penaltyService,
			memberDomainService, emailService, clock
		);
	}

	private Member createMember(String name, String studentNum, String email) {
		return Member.builder()
			.name(name)
			.studentNum(studentNum)
			.email(Email.of(email))
			.build();
	}

	private Reservation createReservationWith(Member member, LocalTime start, LocalTime end, ReservationStatus status) {
		return Reservation.builder()
			.member(member)
			.scheduleDate(LocalDate.of(2025, 4, 4))
			.startTime(start)
			.endTime(end)
			.status(status)
			.build();
	}
}
