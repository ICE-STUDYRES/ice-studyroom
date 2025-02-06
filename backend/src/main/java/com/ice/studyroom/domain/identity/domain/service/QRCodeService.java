package com.ice.studyroom.domain.identity.domain.service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ice.studyroom.domain.reservation.presentation.dto.response.QRDataResponse;

@Service
public class QRCodeService {
	private final StringRedisTemplate redisTemplate;
	private final ObjectMapper objectMapper;
	private static final long EXPIRATION_TIME = 15 * 60 * 60; // QR 코드 유효기간 (15시간)

	public QRCodeService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
		this.redisTemplate = redisTemplate;
		this.objectMapper = objectMapper;
	}

	// 🔹 QR 코드 데이터 저장 (암호화 전 원본을 Key로 저장)
	public void saveQRCode(String email, Long reservationId, String scheduleId, String qrCodeBase64) {
		try {
			String qrKey = "qr:" + email + "_" + reservationId; // 🔹 암호화 전 원본을 Key로 사용

			Map<String, Object> qrData = new HashMap<>();
			qrData.put("qrCodeBase64", qrCodeBase64);
			// 역직렬화 시 Integer로 변환될 것을 방지하기 위해 강제 저장
			qrData.put("reservationId", reservationId.longValue());
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
			return (String)qrData.get("qrCodeBase64");
		} catch (Exception e) {
			throw new RuntimeException("QR 코드 조회 오류", e);
		}
	}

	public QRDataResponse getQRData(String decryptedData) {
		try {
			String jsonValue = redisTemplate.opsForValue().get("qr:" + decryptedData);
			if (jsonValue == null) {
				return null;
			}
			Map<String, Object> qrData = objectMapper.readValue(jsonValue, Map.class);

			Long reservationId = qrData.containsKey("reservationId") ?
				((Number) qrData.get("reservationId")).longValue() : null;
			String email = qrData.containsKey("email") ? (String) qrData.get("email") : null;

			return new QRDataResponse(reservationId, email);
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
