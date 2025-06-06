package com.ice.studyroom.domain.membership.application;

import java.util.Optional;

import com.ice.studyroom.domain.membership.domain.service.encrypt.PasswordEncryptor;
import com.ice.studyroom.domain.membership.domain.service.manage.MemberEmailService;
import com.ice.studyroom.domain.membership.domain.service.manage.MemberPasswordService;
import com.ice.studyroom.domain.membership.domain.vo.EncodedPassword;
import com.ice.studyroom.domain.membership.domain.vo.RawPassword;
import com.ice.studyroom.domain.membership.infrastructure.persistence.MemberRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ice.studyroom.domain.membership.domain.util.MembershipLogUtil;
import com.ice.studyroom.global.security.jwt.JwtToken;
import com.ice.studyroom.domain.identity.domain.application.EmailVerificationService;
import com.ice.studyroom.global.security.service.TokenService;
import com.ice.studyroom.global.security.jwt.JwtTokenProvider;
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
import com.ice.studyroom.domain.penalty.domain.type.PenaltyStatus;
import com.ice.studyroom.domain.penalty.infrastructure.persistence.PenaltyRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MembershipService {
	private final MemberDomainService memberDomainService;
	private final MemberEmailService memberEmailService;
	private final MemberPasswordService memberPasswordService;
	private final PasswordEncryptor passwordEncryptor;
	private final MemberRepository memberRepository;
	private final JwtTokenProvider jwtTokenProvider;
	private final TokenService tokenService;
	private final AuthenticationManager authenticationManager;
	private final EmailVerificationService emailVerificationService;
	private final PenaltyRepository penaltyRepository;

	@Transactional
	public MemberResponse createMember(MemberCreateRequest request) {
		MembershipLogUtil.log("회원가입 요청", "email: " + request.email());
		MembershipLogUtil.log("회원 등록 도메인 로직 진입", "email: " + request.email());

		// 이메일, 비밀번호 유효성 검사 및 VO 생성
		Email email = Email.of(request.email());
		RawPassword rawPassword = RawPassword.of(request.password());
		EncodedPassword encodedPassword = passwordEncryptor.encrypt(rawPassword);

		// 인증된 이메일인지 확인하는 도메인 로직 호출
		memberEmailService.verifyEmailForRegistration(email, request.isAuthenticated(), request.authenticationCode());
		Member member = Member.create(email, encodedPassword, request.name(), request.studentNum());
		memberRepository.save(member);

		MembershipLogUtil.log("회원가입 성공", "email: " + request.email());
		return MemberResponse.of("success");
	}

	@Transactional
	public JwtToken login(MemberLoginRequest request) {
		MembershipLogUtil.log("로그인 요청", "email: " + request.email());

		Member member = memberDomainService.getMemberByEmailForLogin(request.email());
		memberPasswordService.assertPasswordMatches(member, request.password());

		Authentication authentication = authenticationManager.authenticate(
			new UsernamePasswordAuthenticationToken(request.email(), request.password())
		);

		JwtToken jwtToken = jwtTokenProvider.generateToken(authentication);
		tokenService.saveRefreshToken(request.email(), jwtToken.getRefreshToken());

		MembershipLogUtil.log("로그인 성공", "email: " + request.email());
		return jwtToken;
	}

	@Transactional
	public JwtToken refresh(String authorizationHeader, String refreshToken) {
		MembershipLogUtil.log("토큰 리프레시 요청", "Authorization 헤더 존재 여부: " + (authorizationHeader != null));
		String email = tokenService.extractEmailFromAccessToken(authorizationHeader);
		String accessToken = authorizationHeader.replace("Bearer ", "");
		JwtToken token = tokenService.rotateToken(email, accessToken, refreshToken);
		MembershipLogUtil.log("토큰 리프레시 성공", "email: " + email);
		return token;
	}

	@Transactional
	public MemberResponse logout(String authorizationHeader, String refreshToken) {
		String email = tokenService.extractEmailFromAccessToken(authorizationHeader);
		MembershipLogUtil.log("로그아웃 요청", "email: " + email);

		tokenService.deleteToken(email, refreshToken);
		MembershipLogUtil.log("로그아웃 성공", "email: " + email);

		return MemberResponse.of("정상적으로 로그아웃 되었습니다.");
	}

	@Transactional
	public String updatePassword(String authorizationHeader, UpdatePasswordRequest request) {
		String email = tokenService.extractEmailFromAccessToken(authorizationHeader);
		MembershipLogUtil.log("비밀번호 변경 요청", "email: " + email);

		// 비밀번호 유효성 검사 및 VO 생성
		RawPassword current = RawPassword.of(request.currentPassword());
		RawPassword newPass = RawPassword.of(request.updatedPassword());
		RawPassword confirm = RawPassword.of(request.updatedPasswordForCheck());

		// 비밀번호 변경 비즈니스 로직 수행
		Member member = memberDomainService.getMemberByEmail(email);
		memberPasswordService.updateMemberPassword(member, current, newPass, confirm);

		MembershipLogUtil.log("비밀번호 변경 성공", "email: " + email);
		return "비밀번호가 성공적으로 변경되었습니다.";
	}

	public MemberLookupResponse lookUpMember(String authorizationHeader) {
		String email = tokenService.extractEmailFromAccessToken(authorizationHeader);
		MembershipLogUtil.log("회원 정보 조회 요청", "email: " + email);

		String userName = memberDomainService.getUserNameByEmail(Email.of(email));
		Member member = memberDomainService.getMemberByEmail(email);

		Optional<Penalty> penalty = penaltyRepository.findByMemberIdAndStatus(member.getId(), PenaltyStatus.VALID);
		MembershipLogUtil.log("회원 정보 조회 성공", "email: " + email);

		return penalty.map(p -> MemberLookupResponse.of(email, userName, p.getReason(), p.getPenaltyEnd()))
			.orElseGet(() -> MemberLookupResponse.of(email, userName));
	}

	public MemberEmailResponse sendMail(EmailVerificationRequest request) {
		MembershipLogUtil.log("이메일 인증 요청", "email: " + request.email());

		memberEmailService.ensureEmailIsUnique(Email.of(request.email()));
		emailVerificationService.sendCodeToEmail(request.email());

		MembershipLogUtil.log("이메일 인증 코드 전송 완료", "email: " + request.email());
		return MemberEmailResponse.of("인증 메일이 전송되었습니다.");
	}

	public MemberEmailResponse checkEmailVerification(MemberEmailVerificationRequest request) {
		MembershipLogUtil.log("이메일 인증 확인 요청", "email: " + request.email());

		emailVerificationService.verifiedCode(request.email(), request.code());
		MembershipLogUtil.log("이메일 인증 확인 성공", "email: " + request.email());

		return MemberEmailResponse.of("인증이 완료되었습니다.");
	}
}
