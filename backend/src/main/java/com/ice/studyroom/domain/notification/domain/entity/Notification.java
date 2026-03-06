package com.ice.studyroom.domain.notification.domain.entity;

import com.ice.studyroom.domain.notification.type.NotificationEventType;
import com.ice.studyroom.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notification")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "member_id", nullable = false)
	private Long memberId;

	@Enumerated(EnumType.STRING)
	@Column(name = "event_type", nullable = false, length = 30)
	private NotificationEventType eventType;

	@Column(name = "`rank`", nullable = false)
	private int rank;

	@Column(name = "previous_rank")
	private Integer previousRank;

	@Column(nullable = false)
	private int score;

	@Column(name = "is_read", nullable = false)
	private boolean isRead;

	@Column(name = "gap_with_upper")
	private Integer gapWithUpper;

	@Column(name = "event_id", nullable = false, unique = true)
	private String eventId;


	public void markAsRead() {
		this.isRead = true;
	}

	public static Notification create(
		Long memberId,
		NotificationEventType eventType,
		int rank,
		Integer previousRank,
		int score,
		Integer gapWithUpper,
		String eventId
	) {
		Notification n = new Notification();
		n.memberId = memberId;
		n.eventType = eventType;
		n.rank = rank;
		n.previousRank = previousRank;
		n.score = score;
		n.gapWithUpper = gapWithUpper;
		n.eventId = eventId;
		n.isRead = false;

		return n;
	}

}
