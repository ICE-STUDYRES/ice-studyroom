package com.ice.studyroom.domain.texttosql.application;

import com.ice.studyroom.domain.texttosql.domain.entity.SqlExample;
import com.ice.studyroom.domain.texttosql.infrastructure.persistence.SqlExampleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FewShotExampleService {

	private final SqlExampleRepository sqlExampleRepository;

	/**
	 * 사용자 쿼리와 관련된 예제를 찾습니다
	 */
	@Transactional(readOnly = true)
	public List<SqlExample> findRelevantExamples(String userQuery, int limit) {
		log.debug("관련 예제 검색: {}", userQuery);

		// 간단한 키워드 매칭으로 관련 예제 찾기
		List<String> keywords = extractKeywords(userQuery);

		for (String keyword : keywords) {
			List<SqlExample> examples = sqlExampleRepository.findByKeyword(keyword);
			if (!examples.isEmpty()) {
				log.info("키워드 '{}' 매칭 예제 {}개 발견", keyword, examples.size());
				return examples.subList(0, Math.min(limit, examples.size()));
			}
		}

		// 관련 예제 없으면 최근 예제 반환
		log.info("관련 예제 없음. 최근 예제 반환");
		List<SqlExample> recentExamples = sqlExampleRepository.findRecentExamples();
		return recentExamples.subList(0, Math.min(limit, recentExamples.size()));
	}

	/**
	 * 사용자 쿼리에서 키워드 추출
	 */
	private List<String> extractKeywords(String userQuery) {
		List<String> keywords = new ArrayList<>();

		if (userQuery.contains("예약")) {
			keywords.add("예약");
		}
		if (userQuery.contains("회원")) {
			keywords.add("회원");
		}
		if (userQuery.contains("방") || userQuery.contains("스터디룸")) {
			keywords.add("방");
		}
		if (userQuery.contains("오늘") || userQuery.contains("날짜")) {
			keywords.add("날짜");
		}
		if (userQuery.contains("수") || userQuery.contains("통계") || userQuery.contains("집계")) {
			keywords.add("집계");
		}
		if (userQuery.contains("취소")) {
			keywords.add("취소");
		}
		if (userQuery.contains("입실")) {
			keywords.add("입실");
		}

		return keywords;
	}

	/**
	 * Few-shot 예제를 프롬프트 형식으로 변환
	 */
	public String formatExamplesForPrompt(List<SqlExample> examples) {
		if (examples.isEmpty()) {
			return "";
		}

		StringBuilder formatted = new StringBuilder();
		formatted.append("\n## 참고할 예제 (Few-shot Learning):\n\n");

		for (int i = 0; i < examples.size(); i++) {
			SqlExample example = examples.get(i);
			formatted.append(String.format("예제 %d:\n", i + 1));
			formatted.append(String.format("질문: %s\n", example.getUserQuery()));
			formatted.append(String.format("SQL: %s\n", example.getCorrectSql()));
			if (example.getDescription() != null) {
				formatted.append(String.format("설명: %s\n", example.getDescription()));
			}
			formatted.append("\n");
		}

		return formatted.toString();
	}

	/**
	 * 예제 추가 (관리자용)
	 */
	@Transactional
	public SqlExample addExample(String userQuery, String correctSql, String category, String description) {
		SqlExample example = SqlExample.of(userQuery, correctSql, category, description);
		SqlExample saved = sqlExampleRepository.save(example);
		log.info("새 예제 추가: {} (카테고리: {})", userQuery, category);
		return saved;
	}
}
