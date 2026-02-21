package com.ice.studyroom.domain.ranking.scheduler;

import com.ice.studyroom.domain.ranking.application.snapshot.RankingSnapshotJob;
import com.ice.studyroom.domain.ranking.domain.type.RankingPeriod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;

@Slf4j
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
		String periodKey = LocalDate.now(KST).toString();

		log.info("[RANKING] Weekly Snapshot 스케줄 시작 - periodKey: {}", periodKey);

        try {
            snapshotJob.execute(
                RankingPeriod.WEEKLY,
                    periodKey
            );
			log.info("[RANKING] ✅ Weekly Snapshot 스케줄 완료 - periodKey: {}", periodKey);

        } catch (Exception e) {

			log.error("[RANKING] ❌ Weekly Snapshot 스케줄 실패 - periodKey: {}",
					periodKey, e);

			throw e;
        }
    }

	// 월간 - 말일 23:59:59
	@Scheduled(cron = "59 59 23 L * ?", zone = KST_ZONE)
	public void monthlySnapshot() {
		String periodKey = YearMonth.now(KST).toString();

		log.info("[RANKING] Monthly Snapshot 시작 - periodKey: {}", periodKey);

        try {
            snapshotJob.execute(
                RankingPeriod.MONTHLY,
                    periodKey
            );
			log.info("[RANKING] ✅ Monthly Snapshot 완료 - periodKey: {}", periodKey);

        } catch (Exception e) {

			log.error("[RANKING] ❌ Monthly Snapshot 실패 - periodKey: {}",
					periodKey, e);

			throw e;
        }
    }

	// 연간 - 12월 31일 23:59:59
	@Scheduled(cron = "59 59 23 31 12 ?", zone = KST_ZONE)
	public void yearlySnapshot() {
		String periodKey = String.valueOf(LocalDate.now(KST).getYear());

		log.info("[RANKING] Yearly Snapshot 시작 - periodKey: {}", periodKey);

        try {
            snapshotJob.execute(
                RankingPeriod.YEARLY,
                    periodKey
            );
			log.info("[RANKING] ✅ Yearly Snapshot 완료 - periodKey: {}", periodKey);

        } catch (Exception e) {

			log.error("[RANKING] ❌ Yearly Snapshot 실패 - periodKey: {}",
					periodKey, e);

			throw e;
        }
    }

	// 1학기 - 매년 7월 1일 00:00
	@Scheduled(cron = "0 0 0 1 7 ?", zone = KST_ZONE)
	public void firstSemesterSnapshot() {
		String periodKey = LocalDate.now(KST).getYear() + "-1";

		log.info("[RANKING] First Semester Snapshot 시작 - periodKey: {}", periodKey);

        try {
            snapshotJob.execute(
                    RankingPeriod.SEMESTER,
                    periodKey
            );
			log.info("[RANKING] ✅ First Semester Snapshot 완료 - periodKey: {}", periodKey);

        } catch (Exception e) {

			log.error("[RANKING] ❌ First Semester Snapshot 실패 - periodKey: {}",
					periodKey, e);

			throw e;
        }
    }

	// 2학기 - 매년 11월 1일 00:00
	@Scheduled(cron = "0 0 0 1 11 ?", zone = KST_ZONE)
	public void secondSemesterSnapshot() {
		String periodKey = LocalDate.now(KST).getYear() + "-2";

		log.info("[RANKING] Second Semester Snapshot 시작 - periodKey: {}", periodKey);

        try {
            snapshotJob.execute(
                    RankingPeriod.SEMESTER,
                    periodKey
            );
			log.info("[RANKING] ✅ Second Semester Snapshot 완료 - periodKey: {}", periodKey);

        } catch (Exception e) {

			log.error("[RANKING] ❌ Second Semester Snapshot 실패 - periodKey: {}",
					periodKey, e);

			throw e;
        }
    }
}
