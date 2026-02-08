package com.ice.studyroom.domain.ranking.application.checkin;

import com.ice.studyroom.domain.membership.domain.entity.Member;
import com.ice.studyroom.domain.ranking.application.event.RankingContext;
import com.ice.studyroom.domain.ranking.application.event.RankingEventTriggerService;
import com.ice.studyroom.domain.ranking.domain.service.RankingScoreCalculator;
import com.ice.studyroom.domain.ranking.domain.service.RankingStore;
import com.ice.studyroom.domain.ranking.domain.type.RankingPeriod;
import com.ice.studyroom.domain.reservation.domain.entity.Reservation;
import com.ice.studyroom.domain.reservation.domain.type.ReservationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RankingCheckInApplicationService {

	private final RankingScoreCalculator rankingScoreCalculator;
	private final RankingStore rankingStore;
	private final RankingEventTriggerService rankingEventTriggerService;

	private static final int TOP_N = 5;

	public void handleCheckIn(Reservation reservation, ReservationStatus status) {

		// 점수 계산
		int score = rankingScoreCalculator.calculate(reservation, status);

		if (score <= 0) {
			return;
		}

		Long memberId = reservation.getMember().getId();

		// 이벤트 판단용 기준
		RankingPeriod eventPeriod = RankingPeriod.WEEKLY;

		// 점수 반영 전 rank
		Integer previousRank = rankingStore.getRank(eventPeriod, memberId);

		// 기간별 전체 점수 반영
		for (RankingPeriod period : RankingPeriod.values()) {
			rankingStore.increaseScore(period, memberId, score);
		}

		// 점수 반영 후 rank
		Integer currentRank = rankingStore.getRank(eventPeriod, memberId);

		// 이전 랭킹이 없던 사용자의 경우 이벤트 발행 X
		if (previousRank == null || currentRank == null) {
			return;
		}

		// gapWithUpper 계산 (Top5 내부만 의미 있음)
		Integer gapWithUpper = null;

		if (currentRank <= TOP_N) {
			Integer upperScore = rankingStore.getUpperScore(eventPeriod, memberId);
			Integer myScore = rankingStore.getScore(eventPeriod, memberId);

			if (upperScore != null && myScore != null) {
				gapWithUpper = upperScore - myScore;
			}
		}

		// Context 생성
		Member member = reservation.getMember();

		RankingContext context = new RankingContext(
			memberId,
			member.getName(),
			member.getEmail().getValue(),
			previousRank,
			currentRank,
			gapWithUpper
		);

		// 이벤트 트리거
		rankingEventTriggerService.trigger(context);
	}
}
