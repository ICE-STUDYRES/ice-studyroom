package com.ice.studyroom.domain.chatbot.domain.entity;

import com.ice.studyroom.domain.chatbot.domain.type.ChatbotButtonType;
import com.ice.studyroom.domain.chatbot.domain.type.ChatbotEventType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "chatbot_event_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ChatbotEventLog {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "category_id", nullable = false)
	private String categoryId;

	@Column(name = "question_id")
	private Long questionId;

	@Column(name = "event_type", nullable = false)
	@Enumerated(EnumType.STRING)
	private ChatbotEventType eventType;

	@Column(name = "button_type")
	@Enumerated(EnumType.STRING)
	private ChatbotButtonType buttonType;

	@Column(name = "screen")
	private String screen;

	@Column(name = "occurred_at", nullable = false)
	private LocalDateTime occurredAt;

}
