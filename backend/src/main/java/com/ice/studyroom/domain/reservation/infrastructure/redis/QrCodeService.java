package com.ice.studyroom.domain.reservation.infrastructure.redis;

import java.util.concurrent.TimeUnit;

import com.ice.studyroom.domain.reservation.infrastructure.redis.exception.QrTokenNotFoundInCacheException;
import com.ice.studyroom.domain.reservation.util.ReservationLogUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class QrCodeService {

	private final StringRedisTemplate redisTemplate;
	private static final long EXPIRATION_HOURS = 24;
	private static final String QR_CODE_PREFIX = "reservation_id:";

	public QrCodeService(StringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	// reservation_id = qr_token
	public void storeToken(Long reservationId, String qrToken) {
		log.info("QR 토큰 저장 - reservationId: {} = token: {}, 만료: {}시간", reservationId, maskToken(qrToken), EXPIRATION_HOURS);
		redisTemplate.opsForValue().set(
			QR_CODE_PREFIX + reservationId,
			qrToken,
			EXPIRATION_HOURS,
			TimeUnit.HOURS
		);
	}

	// 토큰으로 예약 ID 조회
	public String getTokenByReservationId(Long reservationId) {
		String qrToken = redisTemplate.opsForValue().get(QR_CODE_PREFIX + reservationId);

		if (qrToken == null) {
			ReservationLogUtil.logWarn("QR 입장 실패 - 유효하지 않은 예약 ID: ", reservationId);
			throw new QrTokenNotFoundInCacheException("QR 토큰을 찾을 수 없습니다.");
		}

		log.info("예약 ID로 QR 토큰 조회 - 예약 ID: {}, QR 토큰: {}", reservationId, maskToken(qrToken));
		return qrToken;
	}

	private String maskToken(String token) {
		return token.length() > 6 ? token.substring(0, 6) + "..." : token;
	}
}
