package com.ice.studyroom.domain.schedule.infrastructure.redis;

import com.ice.studyroom.domain.reservation.util.ReservationLogUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Slf4j
@RequiredArgsConstructor
public class ScheduleVacancyAlertService {

	private final RedisTemplate<String, Object> redisTemplate;

	private static final String REDIS_KEY_PREFIX = "vacancy-notification:";

	/**
	 * 특정 스터디룸에 대한 빈자리 알림을 신청합니다.
	 * @param scheduleId 알림을 받을 스케줄 ID
	 * @param email 알림을 받을 사용자의 이메일
	 * @param userName 알림을 받을 사용자의 이름
	 */
	public void registerVacancyAlert(Long scheduleId, String email, String userName) {
		String redisKey = REDIS_KEY_PREFIX + scheduleId;

		redisTemplate.opsForHash().put(redisKey, email, userName);

		if (redisTemplate.getExpire(redisKey) < 0) {
			redisTemplate.expire(redisKey, Duration.ofHours(24));
			ReservationLogUtil.log("새로운 빈자리 알림 키 생성. Key: {}, Expire: 24 hours", redisKey);
		}

		ReservationLogUtil.log("빈자리 알림 등록 완료. Key: {}, Email: {}, User: {}", redisKey, email, userName);
	}
}
