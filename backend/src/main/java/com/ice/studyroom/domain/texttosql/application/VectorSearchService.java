package com.ice.studyroom.domain.texttosql.application;

import com.ice.studyroom.domain.texttosql.domain.entity.TableMetadata;
import com.ice.studyroom.domain.texttosql.infrastructure.redis.RedisVectorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VectorSearchService {

	private final RedisVectorRepository redisVectorRepository;
	private final LocalEmbeddingService localEmbeddingService;

	public Set<String> findRelevantTables(String userQuery, int topK) {
		try {
			log.info("벡터 검색 시작: {}", userQuery);

			// Step 1: 사용자 쿼리를 벡터로 변환
			byte[] queryEmbeddingBytes = localEmbeddingService.createEmbedding(userQuery);
			float[] queryVector = localEmbeddingService.byteArrayToFloatArray(queryEmbeddingBytes);

			// Step 2: 모든 테이블 메타데이터 가져오기
			List<TableMetadata> allTables = redisVectorRepository.findAll();

			// Step 3: 코사인 유사도 계산
			List<TableSimilarity> similarities = new ArrayList<>();
			for (TableMetadata table : allTables) {
				double similarity = cosineSimilarity(queryVector, table.getEmbedding());
				similarities.add(new TableSimilarity(table.getTableName(), similarity));
			}

			// Step 4: 유사도 높은 순으로 정렬
			similarities.sort(Comparator.comparingDouble(TableSimilarity::similarity).reversed());

			// Step 5: Top-K 테이블 선택
			Set<String> relevantTables = similarities.stream()
				.limit(topK)
				.map(TableSimilarity::tableName)
				.collect(Collectors.toSet());

			// Step 6: 관련 테이블도 추가 (JOIN에 필요)
			Set<String> expandedTables = new HashSet<>(relevantTables);
			for (String tableName : relevantTables) {
				redisVectorRepository.findByTableName(tableName).ifPresent(metadata -> {
					if (metadata.getRelatedTables() != null) {
						expandedTables.addAll(metadata.getRelatedTables());
					}
				});
			}

			log.info("벡터 검색 결과 (Top-{}): {}", topK, relevantTables);
			log.info("관련 테이블 포함: {}", expandedTables);

			// 유사도 로그
			similarities.stream()
				.limit(topK)
				.forEach(s -> log.debug("  - {}: {}", s.tableName(), String.format("%.4f", s.similarity())));

			return expandedTables;

		} catch (Exception e) {
			log.error("벡터 검색 실패", e);
			return Set.of("reservation", "member");
		}
	}

	private double cosineSimilarity(float[] vectorA, float[] vectorB) {
		if (vectorA.length != vectorB.length) {
			throw new IllegalArgumentException("벡터 차원이 일치하지 않습니다.");
		}

		double dotProduct = 0.0;
		double normA = 0.0;
		double normB = 0.0;

		for (int i = 0; i < vectorA.length; i++) {
			dotProduct += vectorA[i] * vectorB[i];
			normA += vectorA[i] * vectorA[i];
			normB += vectorB[i] * vectorB[i];
		}

		return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
	}

	private record TableSimilarity(String tableName, double similarity) {}
}
