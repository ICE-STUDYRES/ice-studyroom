package com.ice.studyroom.domain.reservation.application;

import com.ice.studyroom.domain.reservation.domain.exception.reservation.ReservationAccessDeniedException;
import com.ice.studyroom.domain.reservation.domain.exception.reservation.ReservationNotFoundException;
import com.ice.studyroom.domain.reservation.domain.exception.type.reservation.ReservationAccessDeniedReason;
import com.ice.studyroom.domain.reservation.domain.exception.type.reservation.ReservationActionType;
import com.ice.studyroom.global.security.service.TokenService;
import com.ice.studyroom.domain.reservation.domain.entity.Reservation;
import com.ice.studyroom.domain.reservation.infrastructure.persistence.ReservationRepository;
import com.ice.studyroom.domain.reservation.infrastructure.redis.QRCodeService;
import com.ice.studyroom.domain.reservation.infrastructure.util.QRCodeUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class GetMyReservationQrCodeTest {

	@InjectMocks
	private QrEntranceApplicationService qrEntranceApplicationService;

	@Mock private ReservationRepository reservationRepository;
	@Mock private TokenService tokenService;
	@Mock private QRCodeService qrCodeService;
	@Mock private QRCodeUtil qrCodeUtil;

	private Long reservationId;
	private String email;
	private String authHeader;
	private String token;
	private String qrImage;

	@BeforeEach
	void setUp() {
		reservationId = 1L;
		email = "user@hufs.ac.kr";
		authHeader = "Bearer test.token";
		token = "qr-token";
		qrImage = "base64-qr";
	}

	/**
	 * 📌 테스트명: QR_토큰이_없는_예약의_QR_요청은_토큰_생성_및_반환
	 *
	 * ✅ 목적:
	 *   - QR 토큰이 없는 예약 요청 시 토큰이 새로 생성되고 QR 코드가 반환되는지 검증한다.
	 *
	 * 🧪 시나리오 설명:
	 *   1. 사용자 인증 토큰으로 이메일 추출
	 *   2. 예약 ID로 예약 조회 → 존재함
	 *   3. 예약 소유자가 일치함
	 *   4. QR 토큰이 없어서 새로 생성
	 *   5. 생성된 토큰 저장 → Redis 저장 및 QR 코드 생성
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - 예약자는 본인의 예약에 대해서만 QR 코드를 발급받을 수 있음
	 *   - QR 토큰은 없으면 새로 생성되어야 함
	 *
	 * 🧩 검증 포인트:
	 *   - reservation.assignQrToken(...) 호출 여부
	 *   - qrCodeService.storeToken(...)에 동일한 토큰 전달 여부
	 *   - reservationRepository.save(...) 호출 여부
	 *
	 * ✅ 기대 결과:
	 *   - QR 이미지 데이터 반환
	 *   - 토큰 생성 및 저장 완료
	 */
	@Test
	@DisplayName("QR 토큰이 없는 예약의 QR 요청은 토큰 생성 및 반환")
	void QR_토큰이_없는_예약의_QR_요청은_토큰_생성_및_반환() {
		Reservation reservation = 예약_모킹_설정(null, true);
		토큰_추출과_예약_조회_설정(reservation);
		QR_코드_생성_스텁(anyString(), qrImage);

		String expectedToken = "generated-token-123";
		when(reservation.issueQrToken(any())).thenReturn(expectedToken);

		ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);

		String result = qrEntranceApplicationService.getMyReservationQrCode(reservationId, authHeader);

		assertThat(result).isEqualTo(qrImage);

		verify(qrCodeService).storeToken(tokenCaptor.capture(), idCaptor.capture());

		String capturedToken = tokenCaptor.getValue();
		Long capturedId = idCaptor.getValue();

		assertThat(capturedToken).isEqualTo(expectedToken);
		assertThat(capturedId).isEqualTo(reservationId);
	}

	/**
	 * 📌 테스트명: QR_토큰이_이미_있는_예약의_QR_요청은_기존_토큰으로_QR_반환
	 *
	 * ✅ 목적:
	 *   - 이미 QR 토큰이 존재하는 예약은 토큰을 재사용하여 QR 코드를 반환하는지 검증한다.
	 *
	 * 🧪 시나리오 설명:
	 *   1. 예약은 존재하며 사용자가 소유함
	 *   2. QR 토큰이 이미 존재함
	 *   3. 새 토큰 생성 없이 기존 토큰으로 QR 코드 생성
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - QR 토큰이 있으면 새로 생성하지 않고 재사용
	 *
	 * 🧩 검증 포인트:
	 *   - reservationRepository.save(...) 호출되지 않아야 함
	 *   - 기존 토큰으로 QR 생성됨
	 *
	 * ✅ 기대 결과:
	 *   - QR 이미지 반환
	 */
	@Test
	@DisplayName("QR 토큰이 이미 있는 예약은 기존 토큰으로 QR 반환")
	void QR_토큰이_이미_있는_예약은_기존_토큰으로_QR_반환() {
		Reservation reservation = 예약_모킹_설정(token, true);
		토큰_추출과_예약_조회_설정(reservation);
		when(reservation.issueQrToken(any())).thenReturn(token);
		given(qrCodeUtil.generateQRCodeFromToken(token)).willReturn(qrImage);

		String result = qrEntranceApplicationService.getMyReservationQrCode(reservationId, authHeader);

		assertThat(result).isEqualTo(qrImage);
		verify(qrCodeService).storeToken(token, reservationId);
		verify(reservationRepository, never()).save(any());
	}

	/**
	 * 📌 테스트명: 존재하지_않는_예약_ID는_예외_발생
	 *
	 * ✅ 목적:
	 *   - 예약 ID가 잘못되었을 때 예외가 발생하는지 검증한다.
	 *
	 * 🧪 시나리오 설명:
	 *   1. 이메일은 정상적으로 추출됨
	 *   2. reservationRepository.findById() 결과가 empty
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - 존재하지 않는 예약에는 QR을 생성할 수 없음
	 *
	 * 🧩 검증 포인트:
	 *   - BusinessException 예외 발생 여부
	 *
	 * ✅ 기대 결과:
	 *   - 예외 메시지에 "존재하지 않는 예약" 포함
	 */
	@Test
	@DisplayName("존재하지 않는 예약 ID는 예외 발생")
	void 존재하지_않는_예약_ID는_예외_발생() {
		given(tokenService.extractEmailFromAccessToken(authHeader)).willReturn(email);
		given(reservationRepository.findById(reservationId)).willReturn(Optional.empty());

		ReservationNotFoundException ex = assertThrows(ReservationNotFoundException.class, () ->
			qrEntranceApplicationService.getMyReservationQrCode(reservationId, authHeader)
		);
		assertThat(ex.getMessage()).contains("존재하지 않는 예약");
	}

	/**
	 * 📌 테스트명: 예약_소유자가_아닌_경우_예외_발생
	 *
	 * ✅ 목적:
	 *   - 본인의 예약이 아닐 경우 QR 코드 요청이 차단되는지 검증한다.
	 *
	 * 🧪 시나리오 설명:
	 *   1. 이메일과 예약 ID는 존재함
	 *   2. reservation.isOwnedBy(email) → false
	 *
	 * 📌 관련 비즈니스 규칙:
	 *   - QR 요청은 예약 소유자만 가능
	 *
	 * 🧩 검증 포인트:
	 *   - BusinessException 예외 발생 여부
	 *
	 * ✅ 기대 결과:
	 *   - 예외 메시지에 "접근할 수 없습니다" 포함
	 */
	@Test
	@DisplayName("예약 소유자가 아닌 경우 예외 발생")
	void 예약_소유자가_아닌_경우_예외_발생() {
		Reservation reservation = 예약_모킹_설정(null, false);
		토큰_추출과_예약_조회_설정(reservation);

		ReservationAccessDeniedException ex = assertThrows(ReservationAccessDeniedException.class, () ->
			qrEntranceApplicationService.getMyReservationQrCode(reservationId, authHeader)
		);
		assertThat(ex.getMessage()).contains("접근할 수 없습니다");
	}

	private Reservation 예약_모킹_설정(String qrToken, boolean isOwner) {
		Reservation reservation = mock(Reservation.class);
		lenient().when(reservation.getQrToken()).thenReturn(qrToken);
		lenient().when(reservation.getId()).thenReturn(reservationId);
		// isOwnedBy(email) 메서드에 대한 동작을 isOwner 값에 따라 설정
		if (isOwner) {
			// isOwner가 true이면, 예외를 던지지 않음 (성공)
			willDoNothing().given(reservation).validateOwnership(email, ReservationActionType.ISSUE_QR_CODE);
		} else {
			// isOwner가 false이면, 접근 거부 예외를 던짐 (실패)
			willThrow(new ReservationAccessDeniedException(ReservationAccessDeniedReason.NOT_OWNER, reservationId, email, ReservationActionType.ISSUE_QR_CODE))
				.given(reservation).validateOwnership(email, ReservationActionType.ISSUE_QR_CODE);
		}
		lenient().doNothing().when(reservation).validateForQrIssuance();

		return reservation;
	}

	private void 토큰_추출과_예약_조회_설정(Reservation reservation) {
		given(tokenService.extractEmailFromAccessToken(authHeader)).willReturn(email);
		given(reservationRepository.findById(reservationId)).willReturn(Optional.of(reservation));
	}

	private void QR_코드_생성_스텁(String token, String qrImage) {
		given(qrCodeUtil.generateQRCodeFromToken(token)).willReturn(qrImage);
	}
}
