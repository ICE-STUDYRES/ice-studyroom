package com.ice.studyroom.domain.ranking.application.checkin;

import com.ice.studyroom.domain.membership.domain.entity.Member;
import com.ice.studyroom.domain.ranking.application.event.RankingContext;
import com.ice.studyroom.domain.ranking.application.event.RankingEventTriggerService;
import com.ice.studyroom.domain.ranking.domain.service.RankingScoreCalculator;
import com.ice.studyroom.domain.ranking.domain.service.RankingStore;
import com.ice.studyroom.domain.ranking.domain.type.RankingPeriod;
import com.ice.studyroom.domain.reservation.domain.entity.Reservation;
import com.ice.studyroom.domain.reservation.domain.type.ReservationStatus;
import com.ice.studyroom.domain.schedule.domain.entity.Schedule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RankingCheckInApplicationService {

	private final RankingScoreCalculator rankingScoreCalculator;
	private final RankingStore rankingStore;
	private final RankingEventTriggerService rankingEventTriggerService;

	public void handleCheckIn(Reservation reservation, ReservationStatus status) {

		// 점수 계산
		int score = rankingScoreCalculator.calculate(reservation, status);

		applyScoreAndTriggerEvent(reservation, score);
	}

	public void handleExtension(Reservation reservation, Schedule extensionSchedule) {

		int extensionScore = rankingScoreCalculator.calculateForSchedule(extensionSchedule);

		applyScoreAndTriggerEvent(reservation, extensionScore);
	}

	private void applyScoreAndTriggerEvent(Reservation reservation, int score) {

		// 랭킹 점수가 발생하지 않은 경우에는 랭킹 갱신 및 이벤트 처리가 필요 없으므로 return
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

		Integer gapWithUpper = null;
		Integer upperScore = rankingStore.getUpperScore(eventPeriod, memberId);
		Integer myScore = rankingStore.getScore(eventPeriod, memberId);

		if (upperScore != null && myScore != null) {
			gapWithUpper = upperScore - myScore;
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
