package com.ice.studyroom.domain.reservation.application;

import com.ice.studyroom.domain.membership.domain.entity.Member;
import com.ice.studyroom.domain.penalty.application.PenaltyService;
import com.ice.studyroom.domain.penalty.domain.type.PenaltyReasonType;
import com.ice.studyroom.domain.reservation.domain.entity.Reservation;
import com.ice.studyroom.domain.reservation.domain.exception.reservation.ReservationNotFoundException;
import com.ice.studyroom.domain.reservation.domain.exception.type.reservation.ReservationActionType;
import com.ice.studyroom.domain.reservation.domain.exception.type.reservation.ReservationNotFoundReason;
import com.ice.studyroom.domain.reservation.domain.type.ReservationStatus;
import com.ice.studyroom.domain.reservation.infrastructure.persistence.ReservationRepository;
import com.ice.studyroom.domain.reservation.infrastructure.redis.QRCodeService;
import com.ice.studyroom.domain.reservation.infrastructure.util.QRCodeUtil;
import com.ice.studyroom.domain.reservation.presentation.dto.request.QrEntranceRequest;
import com.ice.studyroom.domain.reservation.presentation.dto.response.QrEntranceResponse;
import com.ice.studyroom.domain.reservation.util.ReservationLogUtil;
import com.ice.studyroom.global.exception.token.InvalidQrTokenException;
import com.ice.studyroom.global.security.service.TokenService;
import com.ice.studyroom.global.util.SecureTokenUtil;
import io.lettuce.core.RedisConnectionException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class QrEntranceApplicationService {

	private final TokenService tokenService;
	private final PenaltyService penaltyService;
	private final QRCodeService qrCodeService;
	private final QRCodeUtil qrCodeUtil;
	private final ReservationRepository reservationRepository;
	private final Clock clock;

	@Transactional
	public String getMyReservationQrCode(Long reservationId, String authorizationHeader) {
		ReservationLogUtil.log("QR코드 요청 수신", "예약 ID: " + reservationId);

		String requesterEmail = tokenService.extractEmailFromAccessToken(authorizationHeader);

		// 예약이 유효한지 확인
		Reservation reservation = reservationRepository.findById(reservationId)
			.orElseThrow(() -> {
				return new ReservationNotFoundException(ReservationNotFoundReason.NOT_FOUND, reservationId, requesterEmail, ReservationActionType.ISSUE_QR_CODE);
			});

		reservation.validateForQrIssuance(); // QR 코드를 발급하기 위해 유효한 예약 상태를 가지고 있는지 검증
		reservation.validateOwnership(requesterEmail, ReservationActionType.ISSUE_QR_CODE);	// 요청한 사용자가 해당 예약에 접근할 수 있는지 검증

		String token = reservation.issueQrToken(() -> SecureTokenUtil.generate(10));

		try {
			qrCodeService.storeToken(token, reservation.getId());
		} catch (RedisConnectionException e) {
			ReservationLogUtil.logWarn("Redis 저장 실패, DB만 사용", "예약 ID: " + reservationId);
		}

		String qrCode = qrCodeUtil.generateQRCodeFromToken(token);
		ReservationLogUtil.log("QR 토큰 저장 및 생성 완료", "예약 ID: " + reservationId);
		return qrCode;
	}

	@Transactional
	public QrEntranceResponse qrEntrance(QrEntranceRequest request) {
		String qrToken = request.qrToken();
		ReservationLogUtil.log("QR 입장 요청 수신", "QR 토큰: " + qrToken);

		Reservation reservation = reservationRepository.findByQrToken(qrToken)
			.orElseThrow(() -> new InvalidQrTokenException("유효하지 않은 QR 토큰"));

		// 입장 가능한 예약인지 먼저 확인
		reservation.validateForEntrance();

		// 가능하다면 현재 시간으로 입장 처리 진행
		ReservationStatus status = reservation.processEntrance(LocalDateTime.now(clock));

		// 입실 완료 이후에는 qr 무효화 진행
		qrCodeService.invalidateToken(qrToken);

		Member reservationOwner = reservation.getMember();

		Long reservationId = reservation.getId();
		if(status == ReservationStatus.LATE){
			ReservationLogUtil.logWarn("지각 입장 - 패널티 부여", "예약 ID: " + reservationId,
				"userId: " + reservationOwner.getEmail().getValue());
			penaltyService.assignPenalty(reservationOwner, reservationId, PenaltyReasonType.LATE);
		}

		ReservationLogUtil.log("QR 입장 처리 완료", "예약 ID: " + reservationId);
		return new QrEntranceResponse(status, reservationOwner.getName(), reservationOwner.getStudentNum());
	}
}
