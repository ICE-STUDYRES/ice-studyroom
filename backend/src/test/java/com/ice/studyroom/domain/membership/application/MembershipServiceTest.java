package com.ice.studyroom.domain.membership.application;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import com.ice.studyroom.domain.membership.domain.entity.Member;
import com.ice.studyroom.domain.membership.domain.vo.Email;
import com.ice.studyroom.domain.membership.infrastructure.persistence.MemberRepository;
import com.ice.studyroom.domain.membership.presentation.dto.request.MemberLoginRequest;
import com.ice.studyroom.domain.membership.presentation.dto.response.MemberLoginResponse;
import com.ice.studyroom.global.exception.BusinessException;

import jakarta.transaction.Transactional;

@SpringBootTest
@Transactional
@Rollback
class MembershipServiceTest {

	@Autowired
	private MembershipService membershipService;

	@Autowired
	private MemberRepository memberRepository;

	@Test
	@DisplayName("패널티가 적용된 유저는 로그인 불가")
	void givenPenaltyMember_whenLogin_thenThrowsException() {
		// Given (패널티가 있는 유저)
		Member member = Member.builder()
			.email(new Email("test@hufs.ac.kr"))
			.name("User")
			.password("password")
			.studentNum("67890")
			.roles(List.of("ROLE_USER"))
			.build();

		memberRepository.save(member);

		// When & Then
		MemberLoginRequest request = new MemberLoginRequest("test@hufs.ac.kr", "password");
		BusinessException exception = assertThrows(BusinessException.class,
			() -> membershipService.login(request)
		);
		assertThat(exception.getMessage()).isEqualTo("패널티 상태로 인해 로그인이 제한되었습니다.");
	}
}

