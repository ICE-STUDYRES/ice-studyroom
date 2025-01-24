package com.ice.studyroom.domain.membership.domain.service;

import org.springframework.stereotype.Service;

import com.ice.studyroom.domain.identity.domain.service.VerificationCodeCacheService;
import com.ice.studyroom.domain.membership.domain.vo.Email;
import com.ice.studyroom.domain.membership.infrastructure.persistence.MemberRepository;
import com.ice.studyroom.global.exception.BusinessException;
import com.ice.studyroom.global.type.StatusCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberDomainService {
	private final MemberRepository memberRepository;
	private final VerificationCodeCacheService verificationCodeCacheService;

	public void validateEmailUniqueness(Email email) {
		if (memberRepository.existsByEmail(email)) {
			throw new BusinessException(StatusCode.CONFLICT, "이미 사용 중인 이메일입니다.");
		}
	}

	public void checkVerification(boolean isAuthenticated){
		if(!isAuthenticated){
			throw new BusinessException(StatusCode.BAD_REQUEST, "이메일 인증을 진행해주세요.");
		}
	}

	public void validateVerificationCode(String email, String authenticationCode) {
		if (!verificationCodeCacheService.getVerificationCode(email).equals(authenticationCode)) {
			throw new BusinessException(StatusCode.BAD_REQUEST, "인증 코드가 유효하지 않거나 만료되었습니다.");
		}
	}
}
