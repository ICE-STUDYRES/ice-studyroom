package com.ice.studyroom.domain.texttosql.infrastructure.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ice.studyroom.domain.texttosql.domain.entity.TableMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RedisVectorRepository {

	private final RedisTemplate<String, Object> redisTemplate;
	private final ObjectMapper objectMapper;

	private static final String KEY_PREFIX = "table:";

	/**
	 * 테이블 메타데이터 저장
	 */
	public void save(TableMetadata metadata) {
		try {
			String key = KEY_PREFIX + metadata.getTableName();

			Map<String, String> hash = new HashMap<>();
			hash.put("tableName", metadata.getTableName());
			hash.put("description", metadata.getDescription());
			hash.put("keywords", metadata.getKeywords());
			hash.put("columns", objectMapper.writeValueAsString(metadata.getColumns()));
			hash.put("relatedTables", objectMapper.writeValueAsString(metadata.getRelatedTables()));
			hash.put("embedding", floatArrayToString(metadata.getEmbedding()));

			redisTemplate.opsForHash().putAll(key, hash);

			log.debug("테이블 메타데이터 저장: {}", metadata.getTableName());

		} catch (JsonProcessingException e) {
			log.error("메타데이터 저장 실패", e);
			throw new RuntimeException("메타데이터 저장 실패", e);
		}
	}

	/**
	 * 테이블명으로 조회
	 */
	public Optional<TableMetadata> findByTableName(String tableName) {
		try {
			String key = KEY_PREFIX + tableName;
			Map<Object, Object> hash = redisTemplate.opsForHash().entries(key);

			if (hash.isEmpty()) {
				return Optional.empty();
			}

			return Optional.of(hashToMetadata(hash));

		} catch (Exception e) {
			log.error("메타데이터 조회 실패: {}", tableName, e);
			return Optional.empty();
		}
	}

	/**
	 * 모든 메타데이터 조회
	 */
	public List<TableMetadata> findAll() {
		List<TableMetadata> result = new ArrayList<>();

		Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*");
		if (keys != null) {
			for (String key : keys) {
				Map<Object, Object> hash = redisTemplate.opsForHash().entries(key);
				if (!hash.isEmpty()) {
					try {
						result.add(hashToMetadata(hash));
					} catch (Exception e) {
						log.error("메타데이터 변환 실패: {}", key, e);
					}
				}
			}
		}

		return result;
	}

	/**
	 * 개수 조회
	 */
	public long count() {
		Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*");
		return keys != null ? keys.size() : 0;
	}

	/**
	 * 전체 삭제
	 */
	public void deleteAll() {
		Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*");
		if (keys != null && !keys.isEmpty()) {
			redisTemplate.delete(keys);
		}
	}

	// === Helper Methods ===

	private TableMetadata hashToMetadata(Map<Object, Object> hash) throws JsonProcessingException {
		return TableMetadata.builder()
			.tableName((String) hash.get("tableName"))
			.description((String) hash.get("description"))
			.keywords((String) hash.get("keywords"))
			.columns(objectMapper.readValue((String) hash.get("columns"), List.class))
			.relatedTables(objectMapper.readValue((String) hash.get("relatedTables"), List.class))
			.embedding(stringToFloatArray((String) hash.get("embedding")))
			.build();
	}

	private String floatArrayToString(float[] floats) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < floats.length; i++) {
			if (i > 0) sb.append(",");
			sb.append(floats[i]);
		}
		return sb.toString();
	}

	private float[] stringToFloatArray(String str) {
		String[] parts = str.split(",");
		float[] floats = new float[parts.length];
		for (int i = 0; i < parts.length; i++) {
			floats[i] = Float.parseFloat(parts[i]);
		}
		return floats;
	}
}
