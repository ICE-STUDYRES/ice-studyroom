package com.ice.studyroom.domain.ranking.scheduler;

import com.ice.studyroom.domain.ranking.application.snapshot.RankingSnapshotJob;
import com.ice.studyroom.domain.ranking.domain.entity.RankingConfig;
import com.ice.studyroom.domain.ranking.domain.type.RankingPeriod;
import com.ice.studyroom.domain.ranking.infrastructure.persistence.RankingConfigRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RankingSchedulerTest {

	@Mock
	private RankingSnapshotJob snapshotJob;

	@InjectMocks
	private RankingScheduler scheduler;

	@Test
	void weeklySnapshot_호출되면_execute_호출됨() {

		scheduler.weeklySnapshot();

		verify(snapshotJob).execute(
			eq(RankingPeriod.WEEKLY),
			any()
		);
	}

	@Test
	void monthlySnapshot_호출되면_execute_호출됨() {

		scheduler.monthlySnapshot();

		verify(snapshotJob).execute(
			eq(RankingPeriod.MONTHLY),
			any()
		);
	}

	@Test
	void yearlySnapshot_호출되면_execute_호출됨() {

		scheduler.yearlySnapshot();

		verify(snapshotJob).execute(
			eq(RankingPeriod.YEARLY),
			any()
		);
	}

	@Test
	void firstSemesterSnapshot_호출되면_execute_호출됨() {
		scheduler.firstSemesterSnapshot();

		verify(snapshotJob).execute(
				eq(RankingPeriod.SEMESTER),
				matches("\\d{4}-1")
		);
	}

	@Test
	void secondSemesterSnapshot_호출되면_execute_호출됨() {
		scheduler.secondSemesterSnapshot();

		verify(snapshotJob).execute(
				eq(RankingPeriod.SEMESTER),
				matches("\\d{4}-2")
		);
	}
}
