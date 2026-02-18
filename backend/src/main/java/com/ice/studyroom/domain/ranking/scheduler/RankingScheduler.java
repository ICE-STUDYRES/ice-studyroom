package com.ice.studyroom.domain.ranking.scheduler;

import com.ice.studyroom.domain.ranking.application.snapshot.RankingSnapshotJob;
import com.ice.studyroom.domain.ranking.domain.type.RankingPeriod;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class RankingScheduler {

	private final RankingSnapshotJob snapshotJob;
	private static final String KST_ZONE = "Asia/Seoul";
	private static final ZoneId KST = ZoneId.of(KST_ZONE);

	// 주간 - 일요일 23:59:59
	@Scheduled(cron = "59 59 23 ? * SUN", zone = KST_ZONE)
	public void weeklySnapshot() {
		snapshotJob.execute(
			RankingPeriod.WEEKLY,
			LocalDate.now(KST).toString()
		);
	}

	// 월간 - 말일 23:59:59
	@Scheduled(cron = "59 59 23 L * ?", zone = KST_ZONE)
	public void monthlySnapshot() {
		snapshotJob.execute(
			RankingPeriod.MONTHLY,
			YearMonth.now(KST).toString()
		);
	}

	// 연간 - 12월 31일 23:59:59
	@Scheduled(cron = "59 59 23 31 12 ?", zone = KST_ZONE)
	public void yearlySnapshot() {
		snapshotJob.execute(
			RankingPeriod.YEARLY,
			String.valueOf(LocalDate.now(KST).getYear())
		);
	}

	// 1학기 - 매년 7월 1일 00:00
	@Scheduled(cron = "0 0 0 1 7 ?", zone = KST_ZONE)
	public void firstSemesterSnapshot() {
		snapshotJob.execute(
				RankingPeriod.SEMESTER,
				LocalDate.now(KST).getYear() + "-1"
		);
	}

	// 2학기 - 매년 11월 1일 00:00
	@Scheduled(cron = "0 0 0 1 11 ?", zone = KST_ZONE)
	public void secondSemesterSnapshot() {
		snapshotJob.execute(
				RankingPeriod.SEMESTER,
				LocalDate.now(KST).getYear() + "-2"
		);
	}
}
