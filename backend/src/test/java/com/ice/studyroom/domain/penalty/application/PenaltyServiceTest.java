package com.ice.studyroom.domain.penalty.application;

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
import com.ice.studyroom.domain.penalty.domain.entity.Penalty;
import com.ice.studyroom.domain.penalty.domain.type.PenaltyReasonType;
import com.ice.studyroom.domain.penalty.infrastructure.persistence.PenaltyRepository;

import jakarta.transaction.Transactional;

@SpringBootTest
@Transactional
@Rollback
@DisplayName("PenaltyService 테스트")
class PenaltyServiceTest {

	@Autowired
	private PenaltyService penaltyService;

	@Autowired
	private PenaltyRepository penaltyRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Test
	@DisplayName("assignPenalty 메서드는 멤버에게 패널티를 부여하고 저장해야 한다.")
	void testAssignPenalty() {

		// 1. 테스트 데이터 생성 (멤버)
		Member member = Member.builder()
			.email(new Email("test1@hufs.ac.kr"))
			.name("User1")
			.password("password1")
			.studentNum("12345")
			.roles(List.of("ROLE_USER"))
			.createdAt(LocalDateTime.now())
			.updatedAt(LocalDateTime.now())
			.isPenalty(false)
			.build();

		memberRepository.save(member);

		// 2. 패널티 부여
		penaltyService.assignPenalty(member, 1L, PenaltyReasonType.NO_SHOW);

		// 3. 검증 - 멤버 상태 업데이트
		Member updatedMember = memberRepository.findById(member.getId()).orElseThrow();
		assertThat(updatedMember.isPenalty()).isTrue();

		// 4. 검증 - 패널티 저장 여부
		List<Penalty> penalties = penaltyRepository.findAll();
		assertThat(penalties.size()).isEqualTo(1);

		Penalty penalty = penalties.get(0);
		assertThat(penalty.getMember().getId()).isEqualTo(member.getId());
		assertThat(penalty.getReason()).isEqualTo(PenaltyReasonType.NO_SHOW);

		// 5. 검증 - penaltyEnd 값
		LocalDateTime expectedEndDate = LocalDateTime.now().plusDays(PenaltyReasonType.NO_SHOW.getDurationDays());
		assertThat(penalty.getPenaltyEnd().toLocalDate()).isEqualTo(expectedEndDate.toLocalDate());
	}
}
