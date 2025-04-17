package com.ice.studyroom.domain.identity.domain.application;

import java.security.SecureRandom;

import org.springframework.stereotype.Service;

import com.ice.studyroom.domain.identity.domain.service.VerificationCodeCacheService;
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
	private final VerificationCodeCacheService verificationCodeService;

	public void sendCodeToEmail(String email) {
		if (verificationCodeService.existsVerificationCode(email)) {
			log.warn("이메일 인증 중복 요청 차단 - email: {}", email);
			throw new BusinessException(StatusCode.DUPLICATE_REQUEST, "인증 메일이 이미 발송되었습니다.");
		}

		String title = "[ICE-STUDYRES] 이메일 인증 코드입니다.";
		String authCode = generateVerificationCode();
		String body = buildVerificationEmailBody(authCode);
		try {
			resetTTL(email, authCode);
			emailService.sendEmail(new EmailRequest(email, title, body));
			log.info("이메일 인증 코드 전송 성공 - email: {}", email);  // 추가
		} catch (Exception e) {
			log.error("인증 메일 전송 실패 {}: {}", email, e.getMessage());
			throw new BusinessException(StatusCode.INTERNAL_ERROR, "인증 메일 전송 중 오류가 발생했습니다.");
		}
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

	public void verifiedCode(String email, String authCode) {
		if (!verificationCodeService.existsVerificationCode(email) ||
			!verificationCodeService.getVerificationCode(email).equals(authCode)) {
			log.warn("이메일 인증 실패 - 잘못된 인증 코드 - email: {}", email);
			throw new BusinessException(StatusCode.INVALID_VERIFICATION_CODE, "유효하지 않은 인증코드입니다.");
		}
		log.info("이메일 인증 성공 - email: {}", email);
		resetTTL(email, authCode);
	}

	private void resetTTL(String email, String authCode){
		verificationCodeService.saveVerificationCode(email, authCode);
	}
}
