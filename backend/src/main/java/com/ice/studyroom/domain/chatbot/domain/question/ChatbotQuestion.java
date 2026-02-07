package com.ice.studyroom.domain.chatbot.domain.question;

import com.ice.studyroom.domain.chatbot.domain.category.ChatbotCategory;
import com.ice.studyroom.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chatbot_question")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatbotQuestion extends BaseTimeEntity {

    @Id
    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private ChatbotCategory category;

    @Column(name = "content", length = 255, nullable = false)
    private String content;

    @Column(name = "click_count")
    private Integer clickCount;
}