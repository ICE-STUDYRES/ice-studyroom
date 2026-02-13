package com.ice.studyroom.domain.ranking.scheduler;

import com.ice.studyroom.domain.ranking.application.snapshot.RankingSnapshotJob;
import com.ice.studyroom.domain.ranking.domain.entity.RankingConfig;
import com.ice.studyroom.domain.ranking.domain.type.RankingPeriod;
import com.ice.studyroom.domain.ranking.infrastructure.persistence.RankingConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class RankingScheduler {

	private final RankingSnapshotJob snapshotJob;
	private final RankingConfigRepository rankingConfigRepository;

	// 주간 - 일요일 23:59:59
	@Scheduled(cron = "59 59 23 ? * SUN")
	public void weeklySnapshot() {
		snapshotJob.execute(
			RankingPeriod.WEEKLY,
			LocalDate.now().toString()
		);
	}

	// 월간 - 말일 23:59:59
	@Scheduled(cron = "59 59 23 L * ?")
	public void monthlySnapshot() {
		snapshotJob.execute(
			RankingPeriod.MONTHLY,
			YearMonth.now().toString()
		);
	}

	// 연간 - 12월 31일 23:59:59
	@Scheduled(cron = "59 59 23 31 12 ?")
	public void yearlySnapshot() {
		snapshotJob.execute(
			RankingPeriod.YEARLY,
			String.valueOf(LocalDate.now().getYear())
		);
	}

	// 학기간 -
	@Scheduled(cron = "0 0 0 * * ?")
	public void checkSemesterSnapshot() {

		RankingConfig config =
			rankingConfigRepository.findByPeriod(RankingPeriod.SEMESTER);

		if (config == null || config.getEndAt() == null) return;

		if (!config.isProcessed()
			&& LocalDateTime.now().isAfter(config.getEndAt())) {

			snapshotJob.execute(
				RankingPeriod.SEMESTER,
				config.getEndAt().toLocalDate().toString()
			);

			config.markProcessed(); // 처리 완료 표시
		}
	}


}
