package com.ice.studyroom.domain.ranking.application.query;

import com.ice.studyroom.domain.membership.domain.entity.Member;
import com.ice.studyroom.domain.membership.infrastructure.persistence.MemberRepository;
import com.ice.studyroom.domain.ranking.application.event.assembler.WeeklyRankingAssembler;
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

	private final WeeklyRankingAssembler weeklyRankingAssembler;

	public List<RankingResponse> getTop5(RankingPeriod period) {

		if (period == RankingPeriod.WEEKLY) {
			throw new BusinessException(StatusCode.INVALID_INPUT,
				"WEEKLY는 REST 조회 대상이 아닙니다.");
		}

		return weeklyRankingAssembler.buildTop5(period)
			.stream()
			.map(dto -> new RankingResponse(
				dto.rank(),
				dto.name(),
				dto.score()
			))
			.toList();
	}
}
