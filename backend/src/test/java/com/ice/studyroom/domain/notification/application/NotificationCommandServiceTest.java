package com.ice.studyroom.domain.notification.application;

import com.ice.studyroom.domain.notification.domain.entity.Notification;
import com.ice.studyroom.domain.notification.infrastructure.NotificationRepository;
import com.ice.studyroom.domain.notification.type.NotificationEventType;
import com.ice.studyroom.domain.ranking.application.event.dto.RankingUserChangedEvent;
import com.ice.studyroom.domain.ranking.application.event.policy.RankingEventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class NotificationCommandServiceTest {

	private NotificationRepository notificationRepository;
	private NotificationCommandService notificationCommandService;

	@BeforeEach
	void setUp() {
		notificationRepository = mock(NotificationRepository.class);
		notificationCommandService =
			new NotificationCommandService(notificationRepository);
	}

	private RankingUserChangedEvent createEvent(String eventId) {
		return new RankingUserChangedEvent(
			eventId,
			RankingEventType.TOP5_RANK_CHANGED,
			"weekly-2025-03-20",
			1L,
			"테스트",
			"test@hufs.ac.kr",
			3,
			6,
			120,
			10
		);
	}

	@Test
	@DisplayName("이벤트 수신 시 Notification 정상 저장")
	void saveNotification_success() {

		RankingUserChangedEvent event = createEvent("event-1");

		when(notificationRepository.existsByEventId("event-1"))
			.thenReturn(false);

		notificationCommandService.saveFromRankingEvent(event);

		ArgumentCaptor<Notification> captor =
			ArgumentCaptor.forClass(Notification.class);

		verify(notificationRepository, times(1))
			.save(captor.capture());

		Notification saved = captor.getValue();

		assertThat(saved.getMemberId()).isEqualTo(1L);
		assertThat(saved.getEventType())
			.isEqualTo(NotificationEventType.TOP5_RANK_CHANGED);
		assertThat(saved.getRank()).isEqualTo(3);
		assertThat(saved.getPreviousRank()).isEqualTo(6);
		assertThat(saved.getGapWithUpper()).isEqualTo(10);
		assertThat(saved.isRead()).isFalse();
	}

	@Test
	@DisplayName("같은 eventId가 존재하면 저장하지 않음")
	void duplicateEvent_shouldNotSave() {

		RankingUserChangedEvent event = createEvent("event-1");

		when(notificationRepository.existsByEventId("event-1"))
			.thenReturn(true);

		notificationCommandService.saveFromRankingEvent(event);

		verify(notificationRepository, never())
			.save(any());
	}
}
