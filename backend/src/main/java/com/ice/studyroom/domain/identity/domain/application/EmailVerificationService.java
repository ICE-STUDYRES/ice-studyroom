package com.ice.studyroom.domain.identity.domain.application;

import java.security.SecureRandom;
import java.time.Duration;

import com.ice.studyroom.global.service.CacheService;
import org.springframework.stereotype.Service;

import com.ice.studyroom.global.dto.request.EmailRequest;
import com.ice.studyroom.global.exception.BusinessException;
import com.ice.studyroom.global.service.EmailService;
import com.ice.studyroom.global.type.StatusCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {
	private final EmailService emailService;
	private final CacheService cacheService;

	private static final Duration CODE_TTL = Duration.ofMinutes(5);
	private static final Duration AUTH_TTL = Duration.ofMinutes(10);
	private static final String CODE_PREFIX = "verification:code:";
	private static final String AUTH_PREFIX = "verification:authenticated:";

	/**
	 * 이메일로 인증 코드를 전송합니다.
	 * 6자리 숫자 인증 코드를 생성하여 이메일로 발송하고, Redis에 저장합니다.
	 *
	 * @param email 인증 코드를 받을 이메일 주소
	 * @throws BusinessException 이메일 전송 중 오류가 발생한 경우
	 */
	public void sendCodeToEmail(String email) {
		String title = "[ICE-STUDYRES] 이메일 인증 코드입니다.";
		String authCode = generateVerificationCode();
		String body = buildVerificationEmailBody(authCode);
		try {
			saveVerificationCode(email, authCode);
			emailService.sendEmail(new EmailRequest(email, title, body));
			log.info("이메일 인증 코드 전송 성공 - email: {}", email);  // 추가
		} catch (Exception e) {
			log.error("인증 메일 전송 실패 {}: {}", email, e.getMessage());
			throw new BusinessException(StatusCode.INTERNAL_ERROR, "인증 메일 전송 중 오류가 발생했습니다.");
		}
	}

	/**
	 * 이메일 인증 코드를 검증합니다.
	 * 검증 성공 시 인증 완료 상태를 저장하여 비밀번호 재설정 등에 사용할 수 있도록 합니다.
	 *
	 * @param email 인증할 이메일
	 * @param authCode 사용자가 입력한 인증 코드
	 * @throws BusinessException 인증 코드가 유효하지 않거나 만료된 경우
	 */
	public void verifyCode(String email, String authCode) {
		String savedCode = getVerificationCode(email);

		if (savedCode == null || !savedCode.equals(authCode)) {
			log.warn("이메일 인증 실패 - 잘못된 인증 코드 - email: {}", email);
			throw new BusinessException(StatusCode.INVALID_VERIFICATION_CODE, "인증번호 정보를 다시 확인해주세요.");
		}

		log.info("이메일 인증 성공 - email: {}", email);
		markAsAuthenticated(email);
	}

	/**
	 * 이메일 인증 완료 여부를 확인합니다.
	 *
	 * @param email 확인할 이메일
	 * @throws BusinessException 인증이 완료되지 않은 경우
	 */
	public void ensureEmailAuthenticated(String email) {
		if (!isAuthenticated(email)) {
			log.warn("이메일 인증 미완료 - email: {}", email);
			throw new BusinessException(StatusCode.BAD_REQUEST, "이메일 인증을 완료해주세요.");
		}
	}

	/**
	 * 인증 완료 상태와 인증 코드를 모두 삭제합니다. (재사용 방지)
	 *
	 * @param email 삭제할 이메일
	 */
	public void deleteVerificationStatus(String email) {
		cacheService.delete(AUTH_PREFIX + email);
		cacheService.delete(CODE_PREFIX + email);
		log.info("인증 상태 및 인증 코드 삭제 - email: {}", email);
	}

	private void saveVerificationCode(String email, String code) {
		cacheService.save(CODE_PREFIX + email, code, CODE_TTL);
	}

	private String getVerificationCode(String email) {
		return cacheService.get(CODE_PREFIX + email);
	}

	private void markAsAuthenticated(String email) {
		cacheService.save(AUTH_PREFIX + email, "true", AUTH_TTL);
		log.info("이메일 인증 완료 상태 저장 - email: {}", email);
	}

	private boolean isAuthenticated(String email) {
		return cacheService.exists(AUTH_PREFIX + email);
	}

	private String buildVerificationEmailBody(String authCode) {
		return String.format(
			"<html><body>" +
				"<h2>이메일 인증 번호</h2>" +
				"<p>아래 인증 번호를 입력하여 이메일 인증을 완료하세요.</p>" +
				"<h1 style='color:blue;'>%s</h1>" +
				"<p>감사합니다.</p>" +
				"</body></html>",
			authCode
		);
	}

	private String generateVerificationCode() {
		SecureRandom random = new SecureRandom();
		StringBuilder code = new StringBuilder();
		for (int i = 0; i < 6; i++) {
			code.append(random.nextInt(10));
		}
		return code.toString();
	}
}
