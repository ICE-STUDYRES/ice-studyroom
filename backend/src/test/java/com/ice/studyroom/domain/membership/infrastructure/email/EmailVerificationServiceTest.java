package com.ice.studyroom.domain.membership.infrastructure.email;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ice.studyroom.domain.identity.domain.service.VerificationCodeCacheService;
import com.ice.studyroom.domain.identity.infrastructure.email.EmailVerificationService;
import com.ice.studyroom.global.exception.BusinessException;
import com.ice.studyroom.global.service.CacheService;
import com.ice.studyroom.global.service.EmailService;
import com.ice.studyroom.global.type.StatusCode;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

	private EmailVerificationService emailVerificationService;

	@Mock
	private EmailService emailService;

	@Mock
	private CacheService cacheService;

	@BeforeEach
	void beforeEach(){
		emailVerificationService = new EmailVerificationService(emailService, new VerificationCodeCacheService(cacheService));
	}

	@Test
	@DisplayName("이메일 전송이 성공했을 경우")
	void testSendCodeToEmail() {
		String email = "test@example.com";
		when(cacheService.exists(email)).thenReturn(false); // Redis에 키가 없다고 가정
		doNothing().when(cacheService).save(anyString(), anyString(), eq(Duration.ofMinutes(5)));
		doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());

		emailVerificationService.sendCodeToEmail(email);
		// Assert
		verify(cacheService, times(1)).save(eq(email), anyString(), eq(Duration.ofMinutes(5)));
		verify(emailService, times(1)).sendEmail(eq(email), anyString(), anyString());
	}

	@Test
	@DisplayName("이미 인증 메일이 발송되었으면 에러를 처리해야한다.")
	void testSendCodeToEmail_Failure_EmailAlreadySent() {
		String email = "test@example.com";
		when(cacheService.exists(email)).thenReturn(true); // Redis에 키가 있다고 가정

		BusinessException exception = assertThrows(BusinessException.class, () -> {
			emailVerificationService.sendCodeToEmail(email);
		});

		assertEquals("인증 메일이 이미 발송되었습니다.", exception.getMessage());
		verify(cacheService, never()).save(anyString(), anyString(), eq(Duration.ofMinutes(5))); // save는 호출되지 않아야 함
		verify(emailService, never()).sendEmail(anyString(), anyString(), anyString()); // 메일 발송도 호출되지 않아야 함
	}

	@Test
	@DisplayName("인증 코드가 일치할 경우, 인증이 성공적으로 완료되어야 한다.")
	void testVerifiedCode_Success() {
		String email = "test@example.com";
		String validCode = "123456";

		when(cacheService.exists(email)).thenReturn(true);
		when(cacheService.get(email)).thenReturn(validCode);

		emailVerificationService.verifiedCode(email, validCode);

		verify(cacheService, times(1)).exists(email);
		verify(cacheService, times(1)).get(email);
	}

	@Test
	@DisplayName("인증 코드가 불일치 할 경우, 인증이 실패해야 한다.")
	void testVerifiedCode_Fail() {
		String email = "test@example.com";
		String validCode = "123456";
		String wrongCode = "567890";

		when(cacheService.exists(email)).thenReturn(true);
		when(cacheService.get(email)).thenReturn(wrongCode);

		BusinessException exception = assertThrows(BusinessException.class, () -> {
			emailVerificationService.verifiedCode(email, validCode);
		});

		assertEquals(StatusCode.INVALID_VERIFICATION_CODE, exception.getStatusCode());
		assertEquals("유효하지 않은 인증코드입니다.", exception.getMessage());

		verify(cacheService, times(1)).exists(email);
		verify(cacheService, times(1)).get(email);
	}

	@Test
	@DisplayName("인증코드가 만료되었을 경우")
	void testVerifiedCode_Failure_CodeNotFound() {
		String email = "test@example.com";
		String authCode = "123456";

		when(cacheService.exists(email)).thenReturn(false);

		BusinessException exception = assertThrows(BusinessException.class, () -> {
			emailVerificationService.verifiedCode(email, authCode);
		});

		assertEquals(StatusCode.INVALID_VERIFICATION_CODE, exception.getStatusCode());
		assertEquals("유효하지 않은 인증코드입니다.", exception.getMessage());

		verify(cacheService, times(1)).exists(email);
		verify(cacheService, never()).get(email);
	}
}
