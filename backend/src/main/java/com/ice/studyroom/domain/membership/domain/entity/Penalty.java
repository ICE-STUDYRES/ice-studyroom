package com.ice.studyroom.domain.membership.domain.entity;

import java.time.LocalDateTime;

import com.ice.studyroom.global.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "penalty")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Penalty extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@Column(name = "reason", nullable = false)
	private String reason;

	@Column(name = "penalty_start", nullable = false)
	private LocalDateTime penaltyStart;

	@Column(name = "penalty_end", nullable = false)
	private LocalDateTime penaltyEnd;

	//패널티 무효 속성
	@Column(name = "is_canceled", nullable = false)
	private boolean isCanceled;

	@Builder
	public Penalty(Member member, String reason, LocalDateTime penaltyStart, LocalDateTime penaltyEnd) {
		this.member = member;
		this.reason = reason;
		this.penaltyStart = penaltyStart;
		this.penaltyEnd = penaltyEnd;
		this.isCanceled = false;
	}
}
