package com.ice.studyroom.domain.reservation.infrastructure.kafka;

import com.ice.studyroom.domain.reservation.infrastructure.kafka.dto.VacancyNotificationRequest;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VacancyNotificationProducer {

	private static final String TOPIC_NAME = "vacancy-notifications";
	private static final String KAFKA_PRODUCER_CIRCUIT = "kafka-producer";
	private static final String REDIS_KEY_PREFIX = "vacancy-notification:schedule:";
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");

	private final KafkaTemplate<String, VacancyNotificationRequest> kafkaTemplate;
	private final RedisTemplate<String, Object> redisTemplate;
	private final Clock clock;

	@Async("vacancyNotificationExecutor")
	public void sendVacancyNotificationToSubscribers(Long scheduleId, String roomName, LocalTime startTime, LocalTime endTime) {
		String redisKey = REDIS_KEY_PREFIX + scheduleId;

		Set<Object> subscriberObjects = redisTemplate.opsForHash().keys(redisKey);

		if (subscriberObjects.isEmpty()) {
			log.info("빈자리 알림을 신청한 구독자가 존재하지 않습니다. 스케줄 ID: {}", scheduleId);
			return;
		}

		Set<String> subscribers = subscriberObjects.stream()
			.map(String::valueOf)
			.collect(Collectors.toSet());

		log.info("빈자리 알림 이메일 전송 프로세스 수행, 인원: {} 방 번호: {}", subscribers.size(), roomName);
		String formattedDate = LocalDateTime.now(clock).format(DATE_FORMATTER);

		long currentTimestamp = System.currentTimeMillis();

		String starTimeStr = startTime.format(DateTimeFormatter.ofPattern("HH:mm"));
		String endTimeStr = endTime.format(DateTimeFormatter.ofPattern("HH:mm"));

		for (String email : subscribers) {
			VacancyNotificationRequest request = new VacancyNotificationRequest(
				email,
				roomName,
				formattedDate,
				scheduleId,
				currentTimestamp,
				starTimeStr,
				endTimeStr
			);
			sendSingleNotification(request);
		}
	}

	@CircuitBreaker(name = KAFKA_PRODUCER_CIRCUIT, fallbackMethod = "handleSendFailure")
	public CompletableFuture<Void> sendSingleNotification(VacancyNotificationRequest request) {
		return kafkaTemplate.send(TOPIC_NAME, request)
			.whenComplete((result, ex) -> {
				if (ex == null) {
					log.info("빈자리 알림 이메일 전송 성공. 이메일: {} 토픽: {}",
						request.getEmail(),
						result.getRecordMetadata().topic());
				}
			})
			.thenAccept(sendResult -> {});
	}

	private CompletableFuture<Void> handleSendFailure(VacancyNotificationRequest request, Throwable throwable) {
		if (throwable instanceof CallNotPermittedException) {
			log.error("서킷 브레이커가 OPEN 상태입니다. 카프카 전송을 시도하지 않습니다. Email: {}", request.getEmail());
		} else {
			log.error("빈자리 알림 이메일 전송 실패. 이메일: {} 에러: {}", request.getEmail(), throwable.getMessage());
		}
		return CompletableFuture.completedFuture(null);
	}
}
