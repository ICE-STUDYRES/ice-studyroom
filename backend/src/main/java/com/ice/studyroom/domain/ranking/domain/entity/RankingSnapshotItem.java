package com.ice.studyroom.domain.ranking.domain.entity;

import com.ice.studyroom.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ranking_snapshot_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RankingSnapshotItem extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "snapshot_id", nullable = false)
	private Long snapshotId;

	@Column(name = "member_id", nullable = false)
	private Long memberId;

	@Column(nullable = false)
	private int rank;

	@Column(nullable = false)
	private int score;

	public static RankingSnapshotItem create(
		Long snapshotId,
		Long memberId,
		int rank,
		int score
	) {
		RankingSnapshotItem item = new RankingSnapshotItem();
		item.snapshotId = snapshotId;
		item.memberId = memberId;
		item.rank = rank;
		item.score = score;
		return item;
	}
}
