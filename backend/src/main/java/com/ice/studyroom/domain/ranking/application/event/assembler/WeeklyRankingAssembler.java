package com.ice.studyroom.domain.ranking.application.event.assembler;

import com.ice.studyroom.domain.membership.domain.entity.Member;
import com.ice.studyroom.domain.membership.infrastructure.persistence.MemberRepository;
import com.ice.studyroom.domain.ranking.application.event.dto.WeeklyRankingDto;
import com.ice.studyroom.domain.ranking.domain.service.RankingEntry;
import com.ice.studyroom.domain.ranking.domain.service.RankingStore;
import com.ice.studyroom.domain.ranking.domain.type.RankingPeriod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class WeeklyRankingAssembler {

	private final RankingStore rankingStore;
	private final MemberRepository memberRepository;

	public List<WeeklyRankingDto> buildTop5(RankingPeriod period) {

		List<RankingEntry> entries = rankingStore.getAllRankings(period);

		if (entries.isEmpty()) {
			return List.of();
		}

		List<RankingEntry> top5 = entries.stream()
			.limit(5)
			.toList();

		List<Long> memberIds = top5.stream()
			.map(RankingEntry::memberId)
			.toList();

		Map<Long, Member> memberMap =
			memberRepository.findAllById(memberIds)
				.stream()
				.collect(Collectors.toMap(Member::getId, m -> m));

		return buildDtos(top5, memberMap);
	}

	private List<WeeklyRankingDto> buildDtos(
		List<RankingEntry> entries,
		Map<Long, Member> memberMap
	) {

		List<WeeklyRankingDto> result = new ArrayList<>();

		int previousScore = -1;
		int currentRank = 0;

		for (int i = 0; i < entries.size(); i++) {

			RankingEntry entry = entries.get(i);

			if (entry.score() != previousScore) {
				currentRank = i + 1;
				previousScore = entry.score();
			}

			Member member = memberMap.get(entry.memberId());

			result.add(
				new WeeklyRankingDto(
					currentRank,
					maskName(member.getName()), // 🔥 마스킹은 여기서
					entry.score()
				)
			);
		}

		return result;
	}

	private String maskName(String name) {

		if (name == null || name.isBlank()) {
			return "";
		}

		name = name.trim();
		int length = name.length();

		if (length == 1) {
			return "*";
		}

		if (length == 2) {
			return name.charAt(0) + "*";
		}

		StringBuilder masked = new StringBuilder();
		masked.append(name.charAt(0));

		for (int i = 0; i < length - 2; i++) {
			masked.append("*");
		}

		masked.append(name.charAt(length - 1));

		return masked.toString();
	}
}
