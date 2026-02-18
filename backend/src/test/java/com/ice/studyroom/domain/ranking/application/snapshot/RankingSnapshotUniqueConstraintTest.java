package com.ice.studyroom.domain.ranking.application.snapshot;

import com.ice.studyroom.domain.ranking.domain.type.RankingPeriod;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class RankingSnapshotUniqueConstraintTest {

	@Autowired
	private RankingSnapshotService snapshotService;

	@Test
	void 동일_period와_periodKey로_두번_저장하면_예외발생() {

		// given
		RankingPeriod period = RankingPeriod.SEMESTER;
		String periodKey = "2099-1"; // 미래 값 사용 (충돌 방지)

		List<RankingSnapshotService.SnapshotData> data = List.of(
			new RankingSnapshotService.SnapshotData(1L, 1, 100)
		);

		// when
		snapshotService.createSnapshot(period, periodKey, data);

		// then
		assertThatThrownBy(() ->
			snapshotService.createSnapshot(period, periodKey, data)
		).isInstanceOf(DataIntegrityViolationException.class);
	}
}
