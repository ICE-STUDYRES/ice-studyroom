package com.ice.studyroom.domain.membership.application;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ice.studyroom.domain.membership.domain.entity.Member;
import com.ice.studyroom.domain.membership.domain.service.MemberDomainService;
import com.ice.studyroom.domain.membership.domain.vo.Email;
import com.ice.studyroom.domain.membership.infrastructure.persistence.MemberRepository;
import com.ice.studyroom.domain.membership.presentation.dto.request.MemberCreateRequest;
import com.ice.studyroom.domain.membership.presentation.dto.response.MemberCreateResponse;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class MembershipService {
	private final MemberDomainService memberDomainService;
	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;

	public MemberCreateResponse createUser(MemberCreateRequest request) {
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

		return new MemberCreateResponse("success");
	}
}
