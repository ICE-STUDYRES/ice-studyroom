package com.ice.studyroom.domain.ranking.application.checkin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.ice.studyroom.domain.ranking.application.event.RankingEmailEvent;
import com.ice.studyroom.domain.ranking.application.event.RankingEventPublisher;
import com.ice.studyroom.domain.ranking.domain.service.RankingEventPolicy;

@Service
@RequiredArgsConstructor
public class RankingCheckInService {

	private final RankingEventPolicy rankingEventPolicy;
	private final RankingEventPublisher rankingEventPublisher;

	public void checkIn(Long memberId) {

		int previousRank = getPreviousRank(memberId);
		int currentRank = getCurrentRank(memberId);

		rankingEventPolicy
			.determine(previousRank, currentRank)
			.ifPresent(eventType -> {

				Integer gapWithUpper = calculateGapWithUpper(currentRank);

				RankingEmailEvent event = RankingEmailEvent.of(
					eventType,
					memberId,
					getMemberName(memberId),
					getMemberEmail(memberId),
					currentRank,
					previousRank,
					gapWithUpper
				);

				rankingEventPublisher.publish(event);
			});
	}

	/**
	 * Top5 내부에서만 의미 있음
	 * 1위면 위 순위가 없으므로 null
	 */
	private Integer calculateGapWithUpper(int currentRank) {
		if (currentRank <= 1 || currentRank > 5) {
			return null;
		}
		return getGapWithUpper(); // Redis ZSET 기준으로 추후 구현
	}

	// 임시 메서드

	private int getPreviousRank(Long memberId) {
		return 6; // TODO Redis/ZSET
	}

	private int getCurrentRank(Long memberId) {
		return 5; // TODO Redis/ZSET
	}

	private int getGapWithUpper() {
		return 1; // TODO Redis/ZSET (점수 차 or 시간 차)
	}

	private String getMemberName(Long memberId) {
		return "홍길동"; // TODO membership 연동
	}

	private String getMemberEmail(Long memberId) {
		return "hong@test.com"; // TODO membership 연동
	}
}
