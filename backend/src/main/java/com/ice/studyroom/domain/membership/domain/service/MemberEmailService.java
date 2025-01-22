package com.ice.studyroom.domain.membership.domain.service;

import java.security.SecureRandom;
import java.time.Duration;

import org.springframework.stereotype.Service;

import com.ice.studyroom.global.exception.BusinessException;
import com.ice.studyroom.global.service.EmailService;
import com.ice.studyroom.global.service.RedisService;
import com.ice.studyroom.global.type.StatusCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberEmailService {
	private final EmailService emailService;
	private final RedisService redisService;
	private static final Duration VERIFICATION_CODE_VALIDITY = Duration.ofMinutes(5);

	public void sendCodeToEmail(String email) {
		if (redisService.exists(email)) {
			throw new BusinessException(StatusCode.DUPLICATE_REQUEST, "인증 메일이 이미 발송되었습니다.");
		}

		String title = "[ICE-STUDYRES] 이메일 인증 번호";
		String authCode = generateVerificationCode();
		try {
			redisService.save(email, authCode, VERIFICATION_CODE_VALIDITY);
			emailService.sendEmail(email, title, authCode);
		} catch (Exception e) {
			log.error("인증 메일 전송 실패 {}: {}", email, e.getMessage());
			throw new BusinessException(StatusCode.INTERNAL_ERROR, "인증 메일 전송 중 오류가 발생했습니다.");
		}
	}

	private String generateVerificationCode() {
		SecureRandom random = new SecureRandom();
		StringBuilder code = new StringBuilder();
		for (int i = 0; i < 6; i++) {
			code.append(random.nextInt(10));
		}
		return code.toString();
	}

	public boolean verifiedCode(String email, String authCode) {
		if (!redisService.exists(email) || !redisService.get(email).equals(authCode)) {
			throw new BusinessException(StatusCode.INVALID_VERIFICATION_CODE, "유효하지 않은 인증코드입니다.");
		}
		return true;
	}
}
