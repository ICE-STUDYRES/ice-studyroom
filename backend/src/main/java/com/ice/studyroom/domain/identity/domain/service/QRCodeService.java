package com.ice.studyroom.domain.identity.domain.service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class QRCodeService {
	private final StringRedisTemplate redisTemplate;
	private final ObjectMapper objectMapper;
	private static final long EXPIRATION_TIME = 30; // QR 코드 유효기간 (30분)

	public QRCodeService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
		this.redisTemplate = redisTemplate;
		this.objectMapper = objectMapper;
	}

	// 🔹 QR 코드 데이터 저장 (암호화 전 원본을 Key로 저장)
	public void saveQRCode(String email, String reservationId, String scheduleId, String qrCodeBase64) {
		try {
			String qrKey = "qr:" + email + "_" + reservationId; // 🔹 암호화 전 원본을 Key로 사용

			Map<String, Object> qrData = new HashMap<>();
			qrData.put("qrCodeBase64", qrCodeBase64);
			qrData.put("reservationId", reservationId);
			qrData.put("email", email);
			qrData.put("scheduleId", scheduleId);
			qrData.put("createdAt", Instant.now().toString());
			qrData.put("expiresAt", Instant.now().plusSeconds(EXPIRATION_TIME * 60).toString());

			String jsonValue = objectMapper.writeValueAsString(qrData);
			redisTemplate.opsForValue().set(qrKey, jsonValue, EXPIRATION_TIME, TimeUnit.MINUTES);
		} catch (Exception e) {
			throw new RuntimeException("QR 코드 저장 오류", e);
		}
	}

	// 🔹 QR 코드 검증 (복호화한 데이터가 Redis에 존재하는지 확인)
	public boolean isQRCodeValid(String decryptedData) {
		return redisTemplate.hasKey("qr:" + decryptedData);
	}

	// 🔹 QR 코드 정보 조회
	public String getQRCode(String decryptedData) {
		try {
			String jsonValue = redisTemplate.opsForValue().get("qr:" + decryptedData);
			if (jsonValue == null) {
				return null;
			}
			Map<String, Object> qrData = objectMapper.readValue(jsonValue, Map.class);
			return (String)qrData.get("qrCodeBase64"); // 🔹 특정 필드만 반환
		} catch (Exception e) {
			throw new RuntimeException("QR 코드 조회 오류", e);
		}
	}

	// 🔹 QR 코드 삭제 (만료 시)
	public void deleteQRCode(String decryptedData) {
		redisTemplate.delete("qr:" + decryptedData);
	}

	public String concatEmailResId(String email, String resId) {
		return String.format("%s_%s", email, resId);
	}
}
