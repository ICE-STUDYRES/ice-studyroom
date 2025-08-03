package com.ice.studyroom.domain.reservation.infrastructure.kafka;

import com.ice.studyroom.domain.reservation.infrastructure.kafka.dto.VacancyNotificationRequest;
import com.ice.studyroom.domain.reservation.util.ReservationLogUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
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

	@Async("vacancyNotificationExecutor")
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
			CompletableFuture<SendResult<String, VacancyNotificationRequest>> future = kafkaTemplate.send(TOPIC_NAME, request);

			future.whenComplete((result, ex) -> {
				if (ex == null) {
					ReservationLogUtil.log("빈자리 알림 이메일 전송 성공. " +
						"Email: " + request.getEmail() +
						"Topic: " + result.getRecordMetadata().topic() +
						"Partition: " + result.getRecordMetadata().partition() +
						"Offset: " + result.getRecordMetadata().offset());
				} else {
					ReservationLogUtil.logError("빈자리 알림 이메일 전송 실패. " +
						"Email: " + request.getEmail() +
						"Topic: " + TOPIC_NAME +
						"Error: " + ex.getMessage());
				}
			});
		}
	}
}
