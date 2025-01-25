package com.ice.studyroom.domain.membership.domain.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ice.studyroom.domain.identity.domain.service.VerificationCodeCacheService;
import com.ice.studyroom.domain.membership.domain.entity.Member;
import com.ice.studyroom.domain.membership.domain.vo.Email;
import com.ice.studyroom.domain.membership.infrastructure.persistence.MemberRepository;
import com.ice.studyroom.domain.membership.presentation.dto.request.MemberCreateRequest;
import com.ice.studyroom.global.exception.BusinessException;
import com.ice.studyroom.global.type.StatusCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberDomainService {
	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final VerificationCodeCacheService verificationCodeCacheService;

	public Member registerMember(MemberCreateRequest request) {
		// 1. 이메일 중복 체크
		validateEmailUniqueness(Email.of(request.email()));

		// 2. 이메일 인증 확인
		checkVerification(request.isAuthenticated());

		// 3. 인증 코드 검증
		validateVerificationCode(request.email(), request.authenticationCode());

		// 4. 회원 생성
		Member member = Member.create(
			Email.of(request.email()),
			request.name(),
			request.password(),
			request.studentNum(),
			passwordEncoder
		);

		// 5. 저장
		memberRepository.save(member);

		return member;
	}

	public void validateEmailUniqueness(Email email) {
		if (memberRepository.existsByEmail(email)) {
			throw new BusinessException(StatusCode.CONFLICT, "이미 사용 중인 이메일입니다.");
		}
	}

	public Member getMemberByEmail(String email) {
		return memberRepository.getMemberByEmail(Email.of(email));
	}

	public String getUserNameByEmail(Email email) {
		return memberRepository.findByEmail(email)
			.map(Member::getName)
			.orElseThrow(() -> new BusinessException(StatusCode.NOT_FOUND, "해당 이메일을 가진 유저는 존재하지 않습니다."));
	}

	public void checkVerification(boolean isAuthenticated) {
		if (!isAuthenticated) {
			throw new BusinessException(StatusCode.BAD_REQUEST, "이메일 인증을 진행해주세요.");
		}
	}

	public void validateVerificationCode(String email, String authenticationCode) {
		if (!verificationCodeCacheService.getVerificationCode(email).equals(authenticationCode)) {
			throw new BusinessException(StatusCode.BAD_REQUEST, "인증 코드가 유효하지 않거나 만료되었습니다.");
		}
	}

	public void updateMemberPassword(Member member, String currentPassword, String newPassword,
		String confirmPassword) {
		// 1. 현재 비밀번호 확인
		if (!member.isPasswordValid(currentPassword, passwordEncoder)) {
			throw new BusinessException(StatusCode.UNAUTHORIZED, "기존 비밀번호가 일치하지 않습니다.");
		}

		// 2. 새 비밀번호 검증
		if (!newPassword.equals(confirmPassword)) {
			throw new BusinessException(StatusCode.BAD_REQUEST, "새로운 비밀번호가 서로 일치하지 않습니다.");
		}

		// 3. 새 비밀번호 인코딩 후 업데이트
		member.changePassword(passwordEncoder.encode(newPassword));

		// 4. 저장
		memberRepository.save(member);
	}
}

