package com.ice.studyroom.domain.membership.application;

import java.time.LocalDateTime;
import java.util.Optional;

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
import com.ice.studyroom.domain.membership.presentation.dto.request.EmailVerificationRequest;
import com.ice.studyroom.domain.membership.presentation.dto.request.MemberCreateRequest;
import com.ice.studyroom.domain.membership.presentation.dto.request.MemberEmailVerificationRequest;
import com.ice.studyroom.domain.membership.presentation.dto.request.MemberLoginRequest;
import com.ice.studyroom.domain.membership.presentation.dto.request.UpdatePasswordRequest;
import com.ice.studyroom.domain.membership.presentation.dto.response.MemberEmailResponse;
import com.ice.studyroom.domain.membership.presentation.dto.response.MemberLookupResponse;
import com.ice.studyroom.domain.membership.presentation.dto.response.MemberResponse;
import com.ice.studyroom.domain.penalty.domain.entity.Penalty;
import com.ice.studyroom.domain.penalty.infrastructure.persistence.PenaltyRepository;

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
	private final PenaltyRepository penaltyRepository;

	public MemberResponse createMember(MemberCreateRequest request) {
		memberDomainService.registerMember(request);
		return MemberResponse.of("success");
	}

	public JwtToken login(MemberLoginRequest request) {

		Member member = memberDomainService.getMemberByEmailForLogin(request.email());
		memberDomainService.validatePasswordMatch(member, request.password());

		Authentication authentication = authenticationManager.authenticate(
			new UsernamePasswordAuthenticationToken(request.email(), request.password())
		);

		JwtToken jwtToken = jwtTokenProvider.generateToken(authentication);
		tokenService.saveRefreshToken(request.email(), jwtToken.getRefreshToken());

		return jwtToken;
	}

	public JwtToken refresh(String authorizationHeader, String refreshToken) {
		String email = tokenService.extractEmailFromAccessToken(authorizationHeader);
		String accessToken = authorizationHeader.replace("Bearer ", "");
		return tokenService.rotateToken(email, accessToken, refreshToken);
	}

	public MemberResponse logout(String authorizationHeader, String refreshToken) {
		String email = tokenService.extractEmailFromAccessToken(authorizationHeader);

		tokenService.deleteToken(email, refreshToken);

		return MemberResponse.of("정상적으로 로그아웃 되었습니다.");
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
		Member member = memberDomainService.getMemberByEmail(email);

		// Member의 가장 최근 패널티 조회
		Optional<Penalty> penalty = penaltyRepository.findTopByMemberIdAndPenaltyEndAfterOrderByPenaltyEndDesc(
			member.getId(), LocalDateTime.now());

		if(penalty.isEmpty() || penalty.get().isExpired()) {
			return MemberLookupResponse.of(email, userName);
		}

		return MemberLookupResponse.of(email, userName, penalty.get().getReason(), penalty.get().getPenaltyEnd());
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
