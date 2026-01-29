package com.ice.studyroom.domain.ranking.domain.entity;

import com.ice.studyroom.domain.ranking.domain.type.RankingPeriod;
import com.ice.studyroom.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "ranking_config")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RankingConfig extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private RankingPeriod period;

	@Column(name = "start_at", nullable = false)
	private LocalDateTime startAt;
}
