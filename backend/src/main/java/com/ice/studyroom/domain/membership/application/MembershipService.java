package com.ice.studyroom.domain.membership.application;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ice.studyroom.domain.identity.domain.JwtToken;
import com.ice.studyroom.domain.identity.domain.application.EmailVerificationService;
import com.ice.studyroom.domain.identity.domain.service.TokenService;
import com.ice.studyroom.domain.identity.infrastructure.security.JwtTokenProvider;
import com.ice.studyroom.domain.membership.domain.entity.Member;
import com.ice.studyroom.domain.membership.domain.service.MemberDomainService;
import com.ice.studyroom.domain.membership.domain.vo.Email;
import com.ice.studyroom.domain.membership.infrastructure.persistence.MemberRepository;
import com.ice.studyroom.domain.membership.presentation.dto.request.EmailVerificationRequest;
import com.ice.studyroom.domain.membership.presentation.dto.request.MemberCreateRequest;
import com.ice.studyroom.domain.membership.presentation.dto.request.MemberEmailVerificationRequest;
import com.ice.studyroom.domain.membership.presentation.dto.request.MemberLoginRequest;
import com.ice.studyroom.domain.membership.presentation.dto.request.TokenRequest;
import com.ice.studyroom.domain.membership.presentation.dto.request.UpdatePasswordRequest;
import com.ice.studyroom.domain.membership.presentation.dto.response.MemberEmailResponse;
import com.ice.studyroom.domain.membership.presentation.dto.response.MemberLoginResponse;
import com.ice.studyroom.domain.membership.presentation.dto.response.MemberLookupResponse;
import com.ice.studyroom.domain.membership.presentation.dto.response.MemberResponse;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class MembershipService {
	private final MemberDomainService memberDomainService;
	private final JwtTokenProvider jwtTokenProvider;
	private final TokenService tokenService;
	private final AuthenticationManager authenticationManager;
	private final EmailVerificationService emailVerificationService;

	public MemberResponse createMember(MemberCreateRequest request) {
		memberDomainService.registerMember(request);
		return MemberResponse.of("success");
	}

	public MemberLoginResponse login(MemberLoginRequest request) {
		memberDomainService.checkMemberPenalty(request.email());

		Authentication authentication = authenticationManager.authenticate(
			new UsernamePasswordAuthenticationToken(request.email(), request.password())
		);

		JwtToken jwtToken = jwtTokenProvider.generateToken(authentication);
		tokenService.saveRefreshToken(request.email(), jwtToken.getRefreshToken());

		return MemberLoginResponse.of(jwtToken);
	}

	public MemberLoginResponse refresh(String authorizationHeader, TokenRequest request) {
		String email = tokenService.extractEmailFromAccessToken(authorizationHeader);

		JwtToken jwtToken = tokenService.rotateToken(email, request.refreshToken());

		return MemberLoginResponse.of(jwtToken);
	}

	public MemberResponse logout(String authorizationHeader, TokenRequest request) {
		String email = tokenService.extractEmailFromAccessToken(authorizationHeader);

		tokenService.deleteToken(email, request.refreshToken());

		return MemberResponse.of("success");
	}

	public String updatePassword(String authorizationHeader, UpdatePasswordRequest request) {
		String email = tokenService.extractEmailFromAccessToken(authorizationHeader);
		Member member = memberDomainService.getMemberByEmail(email);
		memberDomainService.updateMemberPassword(member, request.currentPassword(), request.updatedPassword(),
			request.updatedPasswordForCheck());

		return "비밀번호가 성공적으로 변경되었습니다.";
	}

	public MemberLookupResponse lookUpMember(String authorizationHeader) {
		String email = tokenService.extractEmailFromAccessToken(authorizationHeader);
		String userName = memberDomainService.getUserNameByEmail(Email.of(email));
		return MemberLookupResponse.of(email, userName);
	}

	public MemberEmailResponse sendMail(EmailVerificationRequest request) {
		memberDomainService.validateEmailUniqueness(Email.of(request.email()));
		emailVerificationService.sendCodeToEmail(request.email());
		return MemberEmailResponse.of("인증 메일이 전송되었습니다.");
	}

	public MemberEmailResponse checkEmailVerification(MemberEmailVerificationRequest request) {
		emailVerificationService.verifiedCode(request.email(), request.code());
		return MemberEmailResponse.of("인증이 완료되었습니다.");
	}
}
