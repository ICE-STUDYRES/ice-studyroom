package com.ice.studyroom.domain.ranking.application.query;

import com.ice.studyroom.domain.membership.domain.entity.Member;
import com.ice.studyroom.domain.membership.infrastructure.persistence.MemberRepository;
import com.ice.studyroom.domain.ranking.domain.service.RankingEntry;
import com.ice.studyroom.domain.ranking.domain.service.RankingStore;
import com.ice.studyroom.domain.ranking.domain.type.RankingPeriod;
import com.ice.studyroom.domain.ranking.presentation.dto.response.RankingResponse;
import com.ice.studyroom.global.exception.BusinessException;
import com.ice.studyroom.global.type.StatusCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RankingQueryService {

	private final RankingStore rankingStore;
	private final MemberRepository memberRepository;

	public List<RankingResponse> getTop5(RankingPeriod period) {

		if (period == RankingPeriod.WEEKLY) {
			throw new BusinessException(StatusCode.INVALID_INPUT,
				"WEEKLY는 REST 조회 대상이 아닙니다.");
		}

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

		return buildResponses(top5, memberMap);
	}

	private List<RankingResponse> buildResponses(
		List<RankingEntry> entries,
		Map<Long, Member> memberMap
	) {

		List<RankingResponse> result = new ArrayList<>();

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
				new RankingResponse(
					currentRank,
					maskName(member.getName()),
					entry.score()
				)
			);
		}

		return result;
	}

	private String maskName(String name) {
		if (name.length() <= 2) return name;
		return name.charAt(0) + "*" + name.charAt(name.length() - 1);
	}
}
