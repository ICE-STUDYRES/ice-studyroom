package com.ice.studyroom.domain.membership.domain.service.manage;

import com.ice.studyroom.domain.identity.domain.service.VerificationCodeCacheService;
import com.ice.studyroom.domain.membership.domain.util.MembershipLogUtil;
import com.ice.studyroom.domain.membership.domain.vo.Email;
import com.ice.studyroom.domain.membership.infrastructure.persistence.MemberRepository;
import com.ice.studyroom.global.exception.BusinessException;
import com.ice.studyroom.global.type.StatusCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MemberEmailService {

	private final MemberRepository memberRepository;
	private final VerificationCodeCacheService verificationCodeCacheService;

	/**
	 * 회원가입을 위해 이메일 자격을 검증합니다.
	 * 이메일 인증 여부, 이메일 중복 여부, 인증 코드 유효성을 모두 검증합니다.
	 *
	 * @param email               회원가입을 시도하는 사용자의 이메일
	 * @param isAuthenticated     이메일 인증 완료 여부
	 * @param authenticationCode  사용자가 입력한 이메일 인증 코드
	 * @throws BusinessException  인증 실패, 중복 이메일, 인증 코드 불일치 시 예외를 발생시킵니다.
	 */
	public void verifyEmailForRegistration(Email email, boolean isAuthenticated, String authenticationCode) {
		assertEmailAuthenticated(isAuthenticated);
		ensureEmailIsUnique(email);
		verifyVerificationCode(email, authenticationCode);
	}

	/**
	 * 이메일이 DB에 존재하는지 여부를 검증합니다.
	 * 외부 서비스에서도 사용할 수 있는 유틸성 메서드입니다.
	 *
	 * @param email  중복 여부를 검사할 이메일
	 * @throws BusinessException  이메일이 이미 존재할 경우 예외를 발생시킵니다.
	 */
	public void ensureEmailIsUnique(Email email) {
		verifyEmailUniqueness(email);
	}

	/**
	 * 이메일이 인증되었는지를 검증합니다.
	 * 인증되지 않은 경우 예외를 발생시킵니다.
	 *
	 * @param isAuthenticated  이메일 인증 완료 여부
	 * @throws BusinessException  이메일이 인증되지 않은 경우 예외를 발생시킵니다.
	 */
	private void assertEmailAuthenticated(boolean isAuthenticated) {
		if (!isAuthenticated) {
			MembershipLogUtil.logWarn("회원 등록 실패 - 이메일 인증 안됨");
			throw new BusinessException(StatusCode.BAD_REQUEST, "이메일 인증을 진행해주세요.");
		}
	}

	/**
	 * 내부용 이메일 중복 여부 검증입니다.
	 * 주로 외부 메서드를 통해 호출됩니다.
	 *
	 * @param email  중복 여부를 검사할 이메일
	 * @throws BusinessException  이미 존재하는 이메일일 경우 예외를 발생시킵니다.
	 */
	private void verifyEmailUniqueness(Email email) {
		if (memberRepository.existsByEmail(email)) {
			MembershipLogUtil.logWarn("회원 등록 실패 - 중복 이메일", "email: " + email.getValue());
			throw new BusinessException(StatusCode.CONFLICT, "이미 사용 중인 이메일입니다.");
		}
	}

	/**
	 * 이메일 인증 코드의 일치 여부를 검증합니다.
	 * 저장된 인증 코드와 사용자가 입력한 인증 코드를 비교합니다.
	 *
	 * @param email               인증 코드를 확인할 대상 이메일
	 * @param authenticationCode  사용자가 입력한 인증 코드
	 * @throws BusinessException  인증 코드가 유효하지 않거나 만료된 경우 예외를 발생시킵니다.
	 */
	private void verifyVerificationCode(Email email, String authenticationCode) {
		if (!Objects.equals(verificationCodeCacheService.getVerificationCode(email.getValue()), authenticationCode)) {
			MembershipLogUtil.logWarn("회원 등록 실패 - 인증 코드 불일치", "email: " + email);
			throw new BusinessException(StatusCode.BAD_REQUEST, "인증 코드가 유효하지 않거나 만료되었습니다.");
		}
	}
}
