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

	@Mock
	private RankingConfigRepository rankingConfigRepository;

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
	void semester_config없으면_아무일도안함() {

		given(rankingConfigRepository.findByPeriod(RankingPeriod.SEMESTER))
			.willReturn(null);

		scheduler.checkSemesterSnapshot();

		verify(snapshotJob, never()).execute(any(), any());
	}

	@Test
	void semester_endAt없으면_아무일도안함() {

		RankingConfig config = mock(RankingConfig.class);
		given(config.getEndAt()).willReturn(null);

		given(rankingConfigRepository.findByPeriod(RankingPeriod.SEMESTER))
			.willReturn(config);

		scheduler.checkSemesterSnapshot();

		verify(snapshotJob, never()).execute(any(), any());
	}

	@Test
	void semester_종료전이면_실행안함() {

		RankingConfig config = mock(RankingConfig.class);
		given(config.getEndAt())
			.willReturn(LocalDateTime.now().plusDays(1));
		given(config.isProcessed()).willReturn(false);

		given(rankingConfigRepository.findByPeriod(RankingPeriod.SEMESTER))
			.willReturn(config);

		scheduler.checkSemesterSnapshot();

		verify(snapshotJob, never()).execute(any(), any());
	}

	@Test
	void semester_종료됐고_미처리면_실행됨() {

		RankingConfig config = mock(RankingConfig.class);
		given(config.getEndAt())
			.willReturn(LocalDateTime.now().minusDays(1));
		given(config.isProcessed()).willReturn(false);

		given(rankingConfigRepository.findByPeriod(RankingPeriod.SEMESTER))
			.willReturn(config);

		scheduler.checkSemesterSnapshot();

		verify(snapshotJob).execute(
			eq(RankingPeriod.SEMESTER),
			any()
		);

		verify(config).markProcessed();
	}

	@Test
	void semester_이미처리됐으면_실행안함() {

		RankingConfig config = mock(RankingConfig.class);
		given(config.getEndAt())
			.willReturn(LocalDateTime.now().minusDays(1));
		given(config.isProcessed()).willReturn(true);

		given(rankingConfigRepository.findByPeriod(RankingPeriod.SEMESTER))
			.willReturn(config);

		scheduler.checkSemesterSnapshot();

		verify(snapshotJob, never()).execute(any(), any());
	}

}
