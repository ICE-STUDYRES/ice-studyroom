package com.ice.studyroom.domain.membership.application;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ice.studyroom.domain.identity.domain.JwtToken;
import com.ice.studyroom.domain.identity.domain.service.TokenService;
import com.ice.studyroom.domain.identity.infrastructure.security.JwtTokenProvider;
import com.ice.studyroom.domain.membership.domain.entity.Member;
import com.ice.studyroom.domain.membership.domain.service.MemberDomainService;
import com.ice.studyroom.domain.membership.domain.vo.Email;
import com.ice.studyroom.domain.membership.infrastructure.persistence.MemberRepository;
import com.ice.studyroom.domain.membership.presentation.dto.request.MemberCreateRequest;
import com.ice.studyroom.domain.membership.presentation.dto.request.MemberLoginRequest;
import com.ice.studyroom.domain.membership.presentation.dto.request.TokenRequest;
import com.ice.studyroom.domain.membership.presentation.dto.response.MemberLoginResponse;
import com.ice.studyroom.domain.membership.presentation.dto.response.MemberResponse;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class MembershipService {
	private final MemberDomainService memberDomainService;
	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	private final TokenService tokenService;
	private final AuthenticationManager authenticationManager;

	public MemberResponse createMember(MemberCreateRequest request) {
		memberDomainService.validateEmailUniqueness(Email.of(request.email()));

		Member user = Member.builder()
			.email(Email.of(request.email()))
			.name(request.name())
			.password(passwordEncoder.encode(request.password()))
			.studentNum(request.studentNum())
			.roles(List.of("ROLE_USER"))
			.createdAt(LocalDateTime.now())
			.updatedAt(LocalDateTime.now())
			.build();

		memberRepository.save(user);

		return MemberResponse.of("success");
	}

	public MemberLoginResponse login(MemberLoginRequest request) {
		Authentication authentication = authenticationManager.authenticate(
			new UsernamePasswordAuthenticationToken(request.email(), request.password())
		);

		JwtToken jwtToken = jwtTokenProvider.generateToken(authentication);
		tokenService.saveRefreshToken(request.email(), jwtToken.getRefreshToken());

		return MemberLoginResponse.of(jwtToken);
	}

	public MemberLoginResponse refresh(String authorizationHeader, TokenRequest request) {
		// 이메일 정보 추출
		String accessToken = authorizationHeader.replace("Bearer ", "");
		String email = tokenService.extractEmailFromAccessToken(accessToken);

		// tokenService.validateRefreshToken(email, request.refreshToken());
		JwtToken jwtToken = tokenService.rotateToken(email, request.refreshToken());

		return MemberLoginResponse.of(jwtToken);
	}

	public MemberResponse logout(String authorizationHeader, TokenRequest request) {
		String accessToken = authorizationHeader.replace("Bearer ", "");
		String email = tokenService.extractEmailFromAccessToken(accessToken);

		tokenService.deleteToken(email, request.refreshToken());

		return MemberResponse.of("success");
	}
}
