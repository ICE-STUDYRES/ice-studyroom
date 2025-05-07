package com.ice.studyroom.domain.membership.domain.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.ice.studyroom.domain.membership.domain.entity.Member;
import com.ice.studyroom.domain.membership.domain.util.MembershipLogUtil;
import com.ice.studyroom.domain.membership.domain.vo.Email;
import com.ice.studyroom.domain.membership.infrastructure.persistence.MemberRepository;
import com.ice.studyroom.global.exception.BusinessException;
import com.ice.studyroom.global.type.StatusCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberDomainService {
	private final MemberRepository memberRepository;

	public Member getMemberByEmail(String email) {
		return Optional.ofNullable(memberRepository.getMemberByEmail(Email.of(email)))
			.orElseThrow(() -> {
				MembershipLogUtil.logWarn("회원 조회 실패 - 존재하지 않는 이메일", "email: " + email);
				return new BusinessException(StatusCode.NOT_FOUND, "해당 이메일을 가진 유저는 존재하지 않습니다.");
			});
	}

	public Member getMemberByEmailForLogin(String email) {
		return Optional.ofNullable(memberRepository.getMemberByEmail(Email.of(email)))
			.orElseThrow(() -> {
				MembershipLogUtil.logWarn("로그인 실패 - 존재하지 않는 이메일", "email: " + email);
				return new BusinessException(StatusCode.BAD_REQUEST, "아이디 혹은 비밀번호가 일치하지 않습니다.");
			});
	}

	public String getUserNameByEmail(Email email) {
		return memberRepository.findByEmail(email)
			.map(Member::getName)
			.orElseThrow(() -> {
				MembershipLogUtil.logWarn("이름 조회 실패 - 존재하지 않는 이메일", "email: " + email.getValue());
				return new BusinessException(StatusCode.NOT_FOUND, "해당 이메일을 가진 유저는 존재하지 않습니다.");
			});
	}

	public boolean isMemberPenalty(String email) {
		Member member = getMemberByEmail(email);
		return member.isPenalty();
	}

	public List<Member> getMembersByEmail(List<Email> emails){
		return memberRepository.findByEmailIn(emails);
	}
}
