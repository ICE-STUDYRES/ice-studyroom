package com.ice.studyroom.domain.reservation.infrastructure.kafka;

import com.ice.studyroom.domain.reservation.infrastructure.kafka.dto.VacancyNotificationRequest;
import com.ice.studyroom.domain.reservation.util.ReservationLogUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VacancyNotificationProducer {

	private static final String TOPIC_NAME = "vacancy-notifications";
	private static final String REDIS_KEY_PREFIX = "vacancy-notification:schedule:";
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");

	private final KafkaTemplate<String, VacancyNotificationRequest> kafkaTemplate;
	private final RedisTemplate<String, Object> redisTemplate;
	private final Clock clock;

	public void sendVacancyNotificationToSubscribers(Long scheduleId, String roomName) {
		String redisKey = REDIS_KEY_PREFIX + scheduleId;

		Set<Object> subscriberObjects = redisTemplate.opsForHash().keys(redisKey);

		if (subscriberObjects.isEmpty()) {
			ReservationLogUtil.log("빈자리 알림을 신청한 구독자가 존재하지 않습니다. 스케줄 ID: " + scheduleId);
			return;
		}

		Set<String> subscribers = subscriberObjects.stream()
			.map(String::valueOf)
			.collect(Collectors.toSet());

		ReservationLogUtil.log("빈자리 알림 이메일 전송 프로세스 수행, 인원: " + subscribers.size() + "방 번호: " + roomName);
		String formattedDate = LocalDateTime.now(clock).format(DATE_FORMATTER);

		for (String email : subscribers) {
			VacancyNotificationRequest request = new VacancyNotificationRequest(email, roomName, formattedDate);
			kafkaTemplate.send(TOPIC_NAME, request);
			ReservationLogUtil.log("빈자리 알림 이메일 전송. 이메일: " + email);
		}
	}
}
