package com.ice.studyroom.domain.reservation.infrastructure.redis;

import java.util.concurrent.TimeUnit;

import com.ice.studyroom.domain.reservation.util.ReservationLogUtil;
import com.ice.studyroom.global.exception.token.InvalidQrTokenException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class QRCodeService {

	private final StringRedisTemplate redisTemplate;
	private static final long EXPIRATION_HOURS = 24;

	public QRCodeService(StringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	// QR 토큰 저장 Redis에 reservationId 매핑
	public void storeToken(String token, Long reservationId) {
		log.info("QR 토큰 저장 - token: {}, reservationId: {}, 만료: {}시간",  maskToken(token), reservationId, EXPIRATION_HOURS);
		redisTemplate.opsForValue().set(
			"qr:" + token,
			String.valueOf(reservationId),
			EXPIRATION_HOURS,
			TimeUnit.HOURS
		);
	}

	// 토큰으로 예약 ID 조회
	public Long getReservationIdByToken(String token) {
		String value = redisTemplate.opsForValue().get("qr:" + token);
		log.info("QR 토큰으로 예약ID 조회 - token: {}, 결과: {}",  maskToken(token), value);

		if (value == null) {
			ReservationLogUtil.logWarn("QR 입장 실패 - 유효하지 않은 QR 토큰", token);
			throw new InvalidQrTokenException("유효하지않는 QR 코드입니다");
		}

		return Long.parseLong(value);
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
