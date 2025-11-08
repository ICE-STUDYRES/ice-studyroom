package com.ice.studyroom.domain.texttosql.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ice.studyroom.domain.texttosql.application.TextToSqlService.QueryResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueryCacheService {

	private final RedisTemplate<String, String> redisTemplate;
	private final ObjectMapper objectMapper;

	private static final String CACHE_KEY_PREFIX = "text2sql:";
	private static final Duration CACHE_TTL = Duration.ofMinutes(30); // 30분 캐시

	/**
	 * 캐시 키 생성 (사용자 쿼리의 해시)
	 */
	private String generateCacheKey(String userQuery) {
		// 쿼리 정규화 (공백, 대소문자 통일)
		String normalized = userQuery.toLowerCase().trim().replaceAll("\\s+", " ");
		return CACHE_KEY_PREFIX + normalized;
	}

	/**
	 * 캐시에서 결과 조회
	 */
	public Optional<QueryResult> get(String userQuery) {
		try {
			String cacheKey = generateCacheKey(userQuery);
			String cachedJson = redisTemplate.opsForValue().get(cacheKey);

			if (cachedJson != null) {
				log.info("캐시 HIT: {}", userQuery);
				QueryResult result = objectMapper.readValue(cachedJson, QueryResult.class);
				return Optional.of(result);
			}

			log.info("캐시 MISS: {}", userQuery);
			return Optional.empty();

		} catch (JsonProcessingException e) {
			log.error("캐시 역직렬화 실패", e);
			return Optional.empty();
		}
	}

	/**
	 * 결과를 캐시에 저장
	 */
	public void put(String userQuery, QueryResult result) {
		try {
			String cacheKey = generateCacheKey(userQuery);
			String json = objectMapper.writeValueAsString(result);

			redisTemplate.opsForValue().set(cacheKey, json, CACHE_TTL);
			log.info("캐시 저장: {} (TTL: {}분)", userQuery, CACHE_TTL.toMinutes());

		} catch (JsonProcessingException e) {
			log.error("캐시 직렬화 실패", e);
		}
	}

	/**
	 * 특정 쿼리 캐시 삭제
	 */
	public void evict(String userQuery) {
		String cacheKey = generateCacheKey(userQuery);
		redisTemplate.delete(cacheKey);
		log.info("캐시 삭제: {}", userQuery);
	}

	/**
	 * 모든 Text-to-SQL 캐시 삭제
	 */
	public void evictAll() {
		var keys = redisTemplate.keys(CACHE_KEY_PREFIX + "*");
		if (keys != null && !keys.isEmpty()) {
			redisTemplate.delete(keys);
			log.info("전체 캐시 삭제: {}개", keys.size());
		}
	}

	/**
	 * 캐시 통계 조회
	 */
	public CacheStats getStats() {
		var keys = redisTemplate.keys(CACHE_KEY_PREFIX + "*");
		int cacheSize = keys != null ? keys.size() : 0;
		return new CacheStats(cacheSize, CACHE_TTL.toMinutes());
	}

	public record CacheStats(
		int cachedQueries,
		long ttlMinutes
	) {}
}
