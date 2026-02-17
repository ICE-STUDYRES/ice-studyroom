package com.ice.studyroom.domain.ranking.scheduler;

import com.ice.studyroom.domain.ranking.application.snapshot.RankingSnapshotJob;
import com.ice.studyroom.domain.ranking.domain.type.RankingPeriod;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDate;
import java.time.YearMonth;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class RankingScheduler {

	private final RankingSnapshotJob snapshotJob;

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

	// 1학기 - 매년 7월 1일 00:00
	@Scheduled(cron = "0 0 0 1 7 ?")
	public void firstSemesterSnapshot() {
		snapshotJob.execute(
				RankingPeriod.SEMESTER,
				LocalDate.now().getYear() + "-1"
		);
	}

	// 2학기 - 매년 11월 1일 00:00
	@Scheduled(cron = "0 0 0 1 11 ?")
	public void secondSemesterSnapshot() {
		snapshotJob.execute(
				RankingPeriod.SEMESTER,
				LocalDate.now().getYear() + "-2"
		);
	}


}
