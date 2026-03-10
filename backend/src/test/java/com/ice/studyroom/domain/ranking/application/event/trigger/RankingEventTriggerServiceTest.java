package com.ice.studyroom.domain.ranking.application.event.trigger;

import com.ice.studyroom.domain.notification.application.NotificationCommandService;
import com.ice.studyroom.domain.ranking.application.event.assembler.WeeklyRankingAssembler;
import com.ice.studyroom.domain.ranking.application.event.publisher.EventIdGenerator;
import com.ice.studyroom.domain.ranking.application.event.publisher.RankingEventPublisher;
import com.ice.studyroom.domain.ranking.application.event.dto.WeeklyRankingDto;
import com.ice.studyroom.domain.ranking.domain.service.RankingEventPolicy;
import com.ice.studyroom.domain.ranking.domain.service.RankingStore;
import com.ice.studyroom.domain.ranking.domain.type.RankingPeriod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

class RankingEventTriggerServiceTest {

	private final RankingEventPolicy policy = mock(RankingEventPolicy.class);
	private final RankingEventPublisher publisher = mock(RankingEventPublisher.class);
	private final RankingStore rankingStore = mock(RankingStore.class);
	private final WeeklyRankingAssembler assembler = mock(WeeklyRankingAssembler.class);
	private final EventIdGenerator idGenerator = mock(EventIdGenerator.class);
	private final NotificationCommandService notificationCommandService = mock(NotificationCommandService.class);

	private final RankingEventTriggerService service =
		new RankingEventTriggerService(policy, publisher, rankingStore, assembler, idGenerator, notificationCommandService);

	@Test
	@DisplayName("순위 변화가 있으면 알림 저장 후 USER_CHANGED와 LIST_UPDATED 이벤트를 발행한다")
	void trigger_when_rank_changed_publish_events() {

		// given
		RankingContext context = mock(RankingContext.class);

		when(context.previousRank()).thenReturn(5);
		when(context.currentRank()).thenReturn(3);
		when(context.memberId()).thenReturn(1L);
		when(context.memberName()).thenReturn("김예준");
		when(context.memberEmail()).thenReturn("test@test.com");
		when(context.gapWithUpper()).thenReturn(10);

		when(policy.determine(5, 3)).thenReturn(Optional.of(mock()));

		when(rankingStore.getScore(RankingPeriod.WEEKLY, 1L)).thenReturn(100);

		when(assembler.buildTop5(RankingPeriod.WEEKLY))
			.thenReturn(List.of(new WeeklyRankingDto(1, "김*준", 100)));

		when(idGenerator.generate(any())).thenReturn("weekly-2026-02-27-uuid-1234");

		// when
		service.trigger(context);

		// then
		verify(notificationCommandService, times(1))
			.saveFromRankingEvent(any());

		verify(publisher, times(1)).publishUserChanged(any());
		verify(publisher, times(1)).publishListUpdated(any());
	}

	@Test
	@DisplayName("순위 변화가 없으면 이벤트를 발행하지 않는다")
	void trigger_when_no_rank_change_do_nothing() {

		// given
		RankingContext context = mock(RankingContext.class);

		when(context.previousRank()).thenReturn(3);
		when(context.currentRank()).thenReturn(3);

		when(policy.determine(3, 3)).thenReturn(Optional.empty());

		// when
		service.trigger(context);

		// then
		verify(notificationCommandService, never())
			.saveFromRankingEvent(any());

		verify(publisher, never()).publishUserChanged(any());
		verify(publisher, never()).publishListUpdated(any());
	}
}
