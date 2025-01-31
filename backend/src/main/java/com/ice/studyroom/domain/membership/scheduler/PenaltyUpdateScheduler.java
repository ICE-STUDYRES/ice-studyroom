package com.ice.studyroom.domain.membership.scheduler;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ice.studyroom.domain.membership.infrastructure.persistence.MemberRepository;
import com.ice.studyroom.domain.membership.infrastructure.persistence.PenaltyRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class PenaltyUpdateScheduler {

	private final MemberRepository memberRepository;
	private final PenaltyRepository penaltyRepository;

	@Transactional
	@Scheduled(cron = "0 0 0 * * *") // 매일 00:00에 실행
	public void updateMemberPenalty() {
		memberRepository.findAll().forEach(member -> {
			boolean hasPenalty = penaltyRepository
				.findTopByMemberIdAndPenaltyEndAfterOrderByPenaltyEndDesc(
				member.getId(), LocalDateTime.now()).isPresent();


			member.updatePenalty(hasPenalty);
		});

		log.info("Penalty counts updated successfully at {}", LocalDateTime.now());
	}
}
