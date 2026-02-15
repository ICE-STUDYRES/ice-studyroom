package com.ice.studyroom.domain.chatbot.domain.event;

import com.ice.studyroom.domain.chatbot.domain.event.type.ChatbotButtonType;
import com.ice.studyroom.domain.chatbot.domain.event.type.ChatbotEventType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "chatbot_event_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatbotEventLog {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "event_id")
	private Long eventLogId;

	@Column(nullable = false)
	private String categoryId;

	@Column(nullable = true)
	private Long questionId;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private ChatbotEventType eventType;

	@Column(nullable = true)
	@Enumerated(EnumType.STRING)
	private ChatbotButtonType buttonType;

	@Column(nullable = true)
	private String screen;

	@Column(nullable = false)
	private LocalDateTime occurredAt;

	public ChatbotEventLog(ChatbotEventType eventType, ChatbotButtonType buttonType, String categoryId, Long questionId, String screen, LocalDateTime occurredAt) {
		this.categoryId = categoryId;
		this.questionId = questionId;
		this.eventType = eventType;
		this.buttonType = buttonType;
		this.screen = screen;
		this.occurredAt = occurredAt;
	}
}
