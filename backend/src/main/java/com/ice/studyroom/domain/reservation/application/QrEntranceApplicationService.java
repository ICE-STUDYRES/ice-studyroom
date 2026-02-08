package com.ice.studyroom.domain.reservation.application;

import com.ice.studyroom.domain.membership.domain.entity.Member;
import com.ice.studyroom.domain.penalty.application.PenaltyService;
import com.ice.studyroom.domain.penalty.domain.type.PenaltyReasonType;
import com.ice.studyroom.domain.ranking.application.checkin.RankingCheckInApplicationService;
import com.ice.studyroom.domain.reservation.domain.entity.Reservation;
import com.ice.studyroom.domain.reservation.domain.exception.reservation.QrTokenFieldNotFoundException;
import com.ice.studyroom.domain.reservation.domain.exception.reservation.ReservationNotFoundException;
import com.ice.studyroom.domain.reservation.domain.exception.type.reservation.ReservationActionType;
import com.ice.studyroom.domain.reservation.domain.exception.type.reservation.ReservationNotFoundReason;
import com.ice.studyroom.domain.reservation.domain.type.ReservationStatus;
import com.ice.studyroom.domain.reservation.infrastructure.persistence.ReservationRepository;
import com.ice.studyroom.domain.reservation.infrastructure.redis.QrCodeService;
import com.ice.studyroom.domain.reservation.infrastructure.redis.exception.QrTokenNotFoundInCacheException;
import com.ice.studyroom.domain.reservation.infrastructure.util.QRCodeUtil;
import com.ice.studyroom.domain.reservation.presentation.dto.request.QrEntranceRequest;
import com.ice.studyroom.domain.reservation.presentation.dto.response.QrEntranceResponse;
import com.ice.studyroom.domain.reservation.util.ReservationLogUtil;
import com.ice.studyroom.global.security.service.TokenService;
import com.ice.studyroom.global.util.SecureTokenUtil;

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
	private final QrCodeService qrCodeService;
	private final QRCodeUtil qrCodeUtil;
	private final ReservationRepository reservationRepository;
	private final Clock clock;
	private final RankingCheckInApplicationService rankingCheckInApplicationService;


	@Transactional
	public String getMyReservationQrCode(Long reservationId, String authorizationHeader) {
		ReservationLogUtil.log("QR코드 요청 수신", "예약 ID: " + reservationId);

		String requesterEmail = tokenService.extractEmailFromAccessToken(authorizationHeader);

		try {
			String qrToken = qrCodeService.getTokenByReservationId(reservationId);
			return qrCodeUtil.generateQRCodeFromToken(qrToken);
		} catch (QrTokenNotFoundInCacheException e) {
			ReservationLogUtil.log("Redis에서 토큰 미발견", "예약 ID: " + reservationId);
		} catch (Exception e) {
			ReservationLogUtil.logError("Redis 오류로 조회 실패, DB만 사용", "예약 ID: " + reservationId);
		}

		Reservation reservation = reservationRepository.findById(reservationId)
			.orElseThrow(() -> {
				return new ReservationNotFoundException(ReservationNotFoundReason.NOT_FOUND, reservationId, requesterEmail, ReservationActionType.ISSUE_QR_CODE);
			});

		reservation.validateForQrIssuance();
		reservation.validateOwnership(requesterEmail, ReservationActionType.ISSUE_QR_CODE);

		String qrToken = reservation.getQrToken();
		if (qrToken == null) {
			qrToken = reservation.issueQrToken(() -> SecureTokenUtil.generate(10));
		}

		try {
			qrCodeService.storeToken(reservationId, qrToken);
		} catch (Exception e) {
			ReservationLogUtil.logError("Redis 오류로 저장 실패, DB만 사용", "예약 ID: " + reservationId);
		}

		ReservationLogUtil.log("QR 토큰 저장 및 생성 완료", "예약 ID: " + reservationId);
		return qrCodeUtil.generateQRCodeFromToken(qrToken);
	}

	@Transactional
	public QrEntranceResponse qrEntrance(QrEntranceRequest request) {
		String qrToken = request.qrToken();
		ReservationLogUtil.log("QR 입장 요청 수신", "QR 토큰: " + qrToken);

		Reservation reservation = reservationRepository.findByQrToken(qrToken)
			.orElseThrow(() -> new QrTokenFieldNotFoundException("유효하지않은 토큰입니다."));

		// 입장 가능한 예약인지 먼저 확인
		reservation.validateForEntrance();

		// 가능하다면 현재 시간으로 입장 처리 진행
		ReservationStatus status = reservation.processEntrance(LocalDateTime.now(clock));

		if (status == ReservationStatus.ENTRANCE || status == ReservationStatus.LATE) {
			rankingCheckInApplicationService.handleCheckIn(reservation, status);
		}

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
