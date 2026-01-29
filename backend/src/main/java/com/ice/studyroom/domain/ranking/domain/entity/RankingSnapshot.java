package com.ice.studyroom.domain.ranking.domain.entity;

import com.ice.studyroom.domain.ranking.domain.type.RankingPeriod;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ranking_snapshot")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RankingSnapshot {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private RankingPeriod period;

	@Column(name = "period_key", nullable = false, length = 20)
	private String periodKey;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
}
