package com.ice.studyroom.domain.ranking.application.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.ice.studyroom.domain.ranking.domain.service.RankingEventPolicy;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingEventTriggerService {

	private final RankingEventPolicy rankingEventPolicy;
	private final RankingEventPublisher rankingEventPublisher;

	public void trigger(RankingContext context) {

		rankingEventPolicy
			.determine(context.previousRank(), context.currentRank())
			.ifPresent(eventType -> {

                try {

					log.info("[RANKING] 랭킹 이벤트 발생 - memberId: {}, type: {}, 이전: {}, 현재: {}",
							context.memberId(),
							eventType,
							context.previousRank(),
							context.currentRank());

                    RankingEmailEvent event = RankingEmailEvent.of(
                        eventType,
                        context.memberId(),
                        context.memberName(),
                        context.memberEmail(),
                        context.currentRank(),
                        context.previousRank(),
                        context.gapWithUpper()
                    );

                    rankingEventPublisher.publish(event);

                } catch (Exception e) {

					log.error("[RANKING] ❌ 이벤트 발행 실패 - memberId: {}, type: {}",
							context.memberId(),
							eventType,
							e);
                }
            });
	}
}
