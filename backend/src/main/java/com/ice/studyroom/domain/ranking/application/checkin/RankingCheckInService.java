package com.ice.studyroom.domain.ranking.application.checkin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.ice.studyroom.domain.ranking.application.event.RankingEmailEvent;
import com.ice.studyroom.domain.ranking.application.event.RankingEventPublisher;
import com.ice.studyroom.domain.ranking.application.event.RankingEventType;

@Service
@RequiredArgsConstructor
public class RankingCheckInService {

	private final RankingEventPublisher rankingEventPublisher;

	public void checkIn(Long memberId) {

		int previousRank = getPreviousRank(memberId);
		int currentRank = getCurrentRank(memberId);

		// 순위 변동 없으면 이벤트 없음
		if (previousRank == currentRank) {
			return;
		}

		// Top10 밖이면 이벤트 없음
		if (currentRank > 10) {
			return;
		}

		RankingEventType eventType = determineEventType(currentRank);

		Integer gapWithUpper = null;

		if (eventType == RankingEventType.TOP1_ALERT) {
			gapWithUpper = getGapWithSecond();
			if (gapWithUpper > 2) {
				return;
			}
		}

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
	}

	private RankingEventType determineEventType(int rank) {
		if (rank == 1) {
			return RankingEventType.TOP1_ALERT;
		}
		if (rank <= 5) {
			return RankingEventType.TOP2_5_ALERT;
		}
		return RankingEventType.TOP6_10_ALERT;
	}

	// ===== 임시 메서드 =====

	private int getPreviousRank(Long memberId) {
		return 6; // TODO replace
	}

	private int getCurrentRank(Long memberId) {
		return 5; // TODO replace
	}

	private int getGapWithSecond() {
		return 1; // TODO replace (시간 단위)
	}

	private String getMemberName(Long memberId) {
		return "홍길동"; // TODO membership 연동
	}

	private String getMemberEmail(Long memberId) {
		return "hong@test.com"; // TODO membership 연동
	}
}
