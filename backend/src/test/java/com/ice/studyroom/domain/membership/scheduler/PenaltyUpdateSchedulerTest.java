package com.ice.studyroom.domain.membership.scheduler;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import com.ice.studyroom.domain.membership.domain.entity.Member;
import com.ice.studyroom.domain.penalty.domain.entity.Penalty;
import com.ice.studyroom.domain.penalty.domain.type.PenaltyReasonType;
import com.ice.studyroom.domain.membership.domain.vo.Email;
import com.ice.studyroom.domain.membership.infrastructure.persistence.MemberRepository;
import com.ice.studyroom.domain.penalty.infrastructure.persistence.PenaltyRepository;
import com.ice.studyroom.domain.penalty.scheduler.PenaltyUpdateScheduler;

import jakarta.transaction.Transactional;

@SpringBootTest
@Transactional
@Rollback
class PenaltyUpdateSchedulerTest {

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PenaltyRepository penaltyRepository;

	@Autowired
	private PenaltyUpdateScheduler penaltyUpdateScheduler;

	@Test
	@DisplayName("패널티 스케줄러를 통해 Member의 패널티 여부가 갱신되어야한다.")
	void testUpdatePenaltyCounts() {
		// 1. 테스트 데이터를 생성
		Member member1 = Member.builder()
			.email(new Email("test1@hufs.ac.kr"))
			.name("User1")
			.password("password1")
			.studentNum("12345")
			.roles(List.of("ROLE_USER"))
			.createdAt(LocalDateTime.now())
			.updatedAt(LocalDateTime.now())
			.isPenalty(false)
			.build();

		Member member2 = Member.builder()
			.email(new Email("test2@hufs.ac.kr"))
			.name("User2")
			.password("password2")
			.studentNum("67890")
			.roles(List.of("ROLE_USER"))
			.createdAt(LocalDateTime.now())
			.updatedAt(LocalDateTime.now())
			.isPenalty(false)
			.build();

		memberRepository.saveAll(List.of(member1, member2));

		Penalty penalty1 = Penalty.builder()
			.member(member1)
			.reason(PenaltyReasonType.LATE)
			.penaltyEnd(LocalDateTime.now().plusDays(1))
			.build();

		Penalty penalty2 = Penalty.builder()
			.member(member2)
			.reason(PenaltyReasonType.LATE)
			.penaltyEnd(LocalDateTime.now().minusHours(1)) // 만료된 패널티
			.build();

		penaltyRepository.saveAll(List.of(penalty1, penalty2));

		// 2. 스케줄러 실행
		penaltyUpdateScheduler.updateMemberPenalty();

		// 3. 검증
		Member updatedMember1 = memberRepository.findById(member1.getId()).orElseThrow();
		Member updatedMember2 = memberRepository.findById(member2.getId()).orElseThrow();

		assertThat(updatedMember1.isPenalty()).isTrue();
		assertThat(updatedMember2.isPenalty()).isFalse();
	}
}
