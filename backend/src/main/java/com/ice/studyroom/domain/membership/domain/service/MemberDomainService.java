package com.ice.studyroom.domain.membership.domain.service;

import org.springframework.stereotype.Service;

import com.ice.studyroom.domain.membership.domain.vo.Email;
import com.ice.studyroom.domain.membership.infrastructure.persistence.MemberRepository;
import com.ice.studyroom.global.exception.BusinessException;
import com.ice.studyroom.global.type.StatusCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberDomainService {
	private final MemberRepository memberRepository;

	public void validateEmailUniqueness(Email email) {
		if (memberRepository.existsByEmail(email)) {
			throw new BusinessException(StatusCode.DUPLICATE_REQUEST, "이미 사용 중인 이메일입니다.");
		}
	}
}
