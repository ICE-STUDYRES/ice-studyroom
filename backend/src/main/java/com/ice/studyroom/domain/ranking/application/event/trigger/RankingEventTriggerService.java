package com.ice.studyroom.domain.ranking.application.event.trigger;

import com.ice.studyroom.domain.notification.application.NotificationCommandService;
import com.ice.studyroom.domain.ranking.application.event.publisher.EventIdGenerator;
import com.ice.studyroom.domain.ranking.application.event.publisher.RankingEventPublisher;
import com.ice.studyroom.domain.ranking.application.event.assembler.WeeklyRankingAssembler;
import com.ice.studyroom.domain.ranking.application.event.dto.RankingListUpdatedEvent;
import com.ice.studyroom.domain.ranking.application.event.dto.RankingUserChangedEvent;
import com.ice.studyroom.domain.ranking.application.event.dto.WeeklyRankingDto;
import com.ice.studyroom.domain.ranking.domain.service.RankingStore;
import com.ice.studyroom.domain.ranking.domain.type.RankingPeriod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.ice.studyroom.domain.ranking.domain.service.RankingEventPolicy;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingEventTriggerService {

	private final RankingEventPolicy rankingEventPolicy;
	private final RankingEventPublisher rankingEventPublisher;
	private final RankingStore rankingStore;
	private final WeeklyRankingAssembler weeklyRankingAssembler;
	private final EventIdGenerator eventIdGenerator;
	private final NotificationCommandService notificationCommandService;

	private static final RankingPeriod EVENT_PERIOD = RankingPeriod.WEEKLY;
	private static final ZoneId KST = ZoneId.of("Asia/Seoul");

	public void trigger(RankingContext context) {

		rankingEventPolicy
			.determine(context.previousRank(), context.currentRank())
			.ifPresent(eventType -> {

                try {

					String periodKey = EVENT_PERIOD.name().toLowerCase()
							+ "-"
							+ LocalDate.now(KST);

					Integer score = rankingStore.getScore(EVENT_PERIOD, context.memberId());

					// 1. 개인 이벤트 생성
					RankingUserChangedEvent userEvent =
							RankingUserChangedEvent.of(
									eventIdGenerator,
									eventType,
									periodKey,
									context.memberId(),
									context.memberName(),
									context.memberEmail(),
									context.currentRank(),
									context.previousRank(),
									score == null ? 0 : score,
									context.gapWithUpper()
							);

					notificationCommandService.saveFromRankingEvent(userEvent);
					rankingEventPublisher.publishUserChanged(userEvent);

					// 2. 전체 리스트 이벤트 생성
					List<WeeklyRankingDto> top5 =
							weeklyRankingAssembler.buildTop5(EVENT_PERIOD);

					RankingListUpdatedEvent listEvent =
							RankingListUpdatedEvent.of(
									eventIdGenerator,
									periodKey,
									top5
							);

					rankingEventPublisher.publishListUpdated(listEvent);

                } catch (Exception e) {

					log.error("[RANKING] ❌ 이벤트 발행 실패 - memberId: {}, type: {}",
							context.memberId(),
							eventType,
							e);
                }
            });
	}
}
