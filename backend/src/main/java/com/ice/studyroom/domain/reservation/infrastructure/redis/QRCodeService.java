package com.ice.studyroom.domain.reservation.infrastructure.redis;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class QRCodeService {

	private final StringRedisTemplate redisTemplate;
	private static final long EXPIRATION_MINUTES = 15;

	public QRCodeService(StringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	// QR 토큰 저장 Redis에 reservationId 매핑
	public void storeToken(String token, Long reservationId) {
		log.info("QR 토큰 저장 - token: {}, reservationId: {}, 만료: {}분",  maskToken(token), reservationId, EXPIRATION_MINUTES);
		redisTemplate.opsForValue().set(
			"qr:" + token,
			String.valueOf(reservationId),
			EXPIRATION_MINUTES,
			TimeUnit.MINUTES
		);
	}

	// 토큰으로 예약 ID 조회
	public Long getReservationIdByToken(String token) {
		String value = redisTemplate.opsForValue().get("qr:" + token);
		log.info("QR 토큰으로 예약ID 조회 - token: {}, 결과: {}",  maskToken(token), value);
		return value != null ? Long.parseLong(value) : null;
	}

	// 유효성 확인이 필요할 경우 처리
	public boolean isValidToken(String token) {
		boolean result = redisTemplate.hasKey("qr:" + token);
		log.info("QR 토큰 유효성 검사 - token: {}, 유효: {}",  maskToken(token), result);
		return result;
	}

	// 1회 사용 이후에 토큰 삭제
	public void invalidateToken(String token) {
		log.info("QR 토큰 삭제 - token: {}", maskToken(token));
		redisTemplate.delete("qr:" + token);
	}

	private String maskToken(String token) {
		return token.length() > 6 ? token.substring(0, 6) + "..." : token;
	}
}
