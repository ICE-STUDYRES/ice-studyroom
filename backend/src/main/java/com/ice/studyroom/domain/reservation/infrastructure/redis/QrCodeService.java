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
	private static final String QR_KEY_PREFIX = "reservation_id:";

	public QrCodeService(StringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	// QR 토큰 저장 Redis에 reservationId 매핑
	public void storeToken(Long reservationId, String token) {
		log.info("QR 토큰 저장 - reservationId: {} = token: {}, 만료: {}시간", reservationId, maskToken(token), EXPIRATION_HOURS);
		redisTemplate.opsForValue().set(
			QR_KEY_PREFIX + reservationId,
			token,
			EXPIRATION_HOURS,
			TimeUnit.HOURS
		);
	}

	// 토큰으로 예약 ID 조회
	public String getTokenByReservationId(Long reservationId) {
		String qrToken = redisTemplate.opsForValue().get(QR_KEY_PREFIX + reservationId);

		if (qrToken == null) {
			ReservationLogUtil.logWarn("예약 ID에 해당하는 QR 토큰이 캐시에 존재하지않습니다. 예약 ID: ", reservationId);
			throw new QrTokenNotFoundInCacheException("[캐시 미스 발생] 예약 ID가 존재하지않습니다.");
		}
		log.info("QR 토큰으로 예약ID 조회 - token: {}, 결과: {}",  maskToken(qrToken), qrToken);

		return qrToken;
	}

	private String maskToken(String token) {
		return token.length() > 6 ? token.substring(0, 6) + "..." : token;
	}
}
