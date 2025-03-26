package com.ice.studyroom.domain.reservation.infrastructure.redis;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class QRCodeService {

	private final StringRedisTemplate redisTemplate;
	private static final long EXPIRATION_MINUTES = 15;

	public QRCodeService(StringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	// QR 토큰 저장 Redis에 reservationId 매핑
	public void storeToken(String token, Long reservationId) {
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
		return value != null ? Long.parseLong(value) : null;
	}

	// 유효성 확인이 필요할 경우 처리
	public boolean isValidToken(String token) {
		return redisTemplate.hasKey("qr:" + token);
	}

	// 1회 사용 이후에 토큰 삭제
	public void invalidateToken(String token) {
		redisTemplate.delete("qr:" + token);
	}
}
