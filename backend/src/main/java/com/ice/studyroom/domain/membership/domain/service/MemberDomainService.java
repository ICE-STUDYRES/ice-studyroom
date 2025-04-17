package com.ice.studyroom.domain.membership.domain.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberDomainService {
	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final VerificationCodeCacheService verificationCodeCacheService;

	public void registerMember(MemberCreateRequest request) {
		log.info("회원 등록 도메인 로직 진입 - email: {}", request.email());
		validateEmailUniqueness(Email.of(request.email()));
		checkVerification(request.isAuthenticated());
		validateVerificationCode(request.email(), request.authenticationCode());
		//validateStudentNumberUniqueness(request.studentNum());

		Member member = Member.create(
			Email.of(request.email()),
			request.name(),
			request.password(),
			request.studentNum(),
			passwordEncoder
		);

		memberRepository.save(member);
	}

	public void validateEmailUniqueness(Email email) {
		if (memberRepository.existsByEmail(email)) {
			log.warn("회원 등록 실패 - 중복 이메일 - email: {}", email.getValue());
			throw new BusinessException(StatusCode.CONFLICT, "이미 사용 중인 이메일입니다.");
		}
	}

	// private void validateStudentNumberUniqueness(String studentNum) {
	// 	if (memberRepository.existsByStudentNum(studentNum)) {
	// 		throw new BusinessException(StatusCode.CONFLICT, "이미 사용 중인 학번입니다.");
	// 	}
	// }

	public Member getMemberByEmail(String email) {
		return Optional.ofNullable(memberRepository.getMemberByEmail(Email.of(email)))
			.orElseThrow(() -> {
				log.warn("회원 조회 실패 - 존재하지 않는 이메일 - email: {}", email);
				return new BusinessException(StatusCode.NOT_FOUND, "해당 이메일을 가진 유저는 존재하지 않습니다.");
			});
	}

	public List<Member> getMembersByEmail(List<Email> emails){
		return memberRepository.findByEmailIn(emails);
	}

	public Member getMemberByEmailForLogin(String email) {
		return Optional.ofNullable(memberRepository.getMemberByEmail(Email.of(email)))
			.orElseThrow(() -> {
				log.warn("로그인 실패 - 존재하지 않는 이메일 - email: {}", email);
				return new BusinessException(StatusCode.BAD_REQUEST, "아이디 혹은 비밀번호가 일치하지 않습니다.");
			});
	}

	public void validatePasswordMatch(Member member, String password){
		if(!member.isPasswordValid(password, passwordEncoder)){
			log.warn("로그인 실패 - 비밀번호 불일치 - email: {}", member.getEmail().getValue());
			throw new BusinessException(StatusCode.BAD_REQUEST, "아이디 혹은 비밀번호가 일치하지 않습니다.");
		}
	}

	public String getUserNameByEmail(Email email) {
		return memberRepository.findByEmail(email)
			.map(Member::getName)
			.orElseThrow(() -> {
				log.warn("이름 조회 실패 - 존재하지 않는 이메일 - email: {}", email.getValue());
				return new BusinessException(StatusCode.NOT_FOUND, "해당 이메일을 가진 유저는 존재하지 않습니다.");
			});
	}

	private void checkVerification(boolean isAuthenticated) {
		if (!isAuthenticated) {
			log.warn("회원 등록 실패 - 이메일 인증 안됨");
			throw new BusinessException(StatusCode.BAD_REQUEST, "이메일 인증을 진행해주세요.");
		}
	}

	private void validateVerificationCode(String email, String authenticationCode) {
		if (!Objects.equals(verificationCodeCacheService.getVerificationCode(email), authenticationCode)) {
			log.warn("회원 등록 실패 - 인증 코드 불일치 - email: {}", email);
			throw new BusinessException(StatusCode.BAD_REQUEST, "인증 코드가 유효하지 않거나 만료되었습니다.");
		}
	}

	public void updateMemberPassword(Member member, String currentPassword, String newPassword,
		String confirmPassword) {
		if (!member.isPasswordValid(currentPassword, passwordEncoder)) {
			log.warn("비밀번호 변경 실패 - 기존 비밀번호 불일치 - userEmail: {}", member.getEmail().getValue());
			throw new BusinessException(StatusCode.UNAUTHORIZED, "기존 비밀번호가 일치하지 않습니다.");
		}

		if(newPassword.equals(currentPassword)) {
			log.warn("비밀번호 변경 실패 - 새로운 비밀번호가 기존 비밀번호와 동일함 - userEmail: {}", member.getEmail().getValue());
			throw new BusinessException(StatusCode.BAD_REQUEST, "기존 비밀번호와 새로운 비밀번호가 동일합니다.");
		}

		if (!newPassword.equals(confirmPassword)) {
			log.warn("비밀번호 변경 실패 - 새 비밀번호가 일치하지 않음 - userEmail: {}", member.getEmail().getValue());
			throw new BusinessException(StatusCode.BAD_REQUEST, "새로운 비밀번호가 서로 일치하지 않습니다.");
		}

		member.changePassword(passwordEncoder.encode(newPassword));

		memberRepository.save(member);
	}

	public boolean isMemberPenalty(String email) {
		Member member = getMemberByEmail(email);
		return member.isPenalty();
	}
}
