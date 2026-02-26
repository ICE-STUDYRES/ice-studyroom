package com.ice.studyroom.domain.chatbot.domain.entity;

import com.ice.studyroom.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chatbot_category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatbotCategory extends BaseTimeEntity {

    @Id
    private String id;

    @Column(name = "label", length = 50, nullable = false)
    private String label;

	@Column(name = "route", length = 100)
	private String route;

	@Column(name = "notion_url", length = 255)
	private String notionUrl;
}
