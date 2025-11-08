package com.ice.studyroom.domain.texttosql.infrastructure.persistence;

import com.ice.studyroom.domain.texttosql.domain.entity.SqlExample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SqlExampleRepository extends JpaRepository<SqlExample, Long> {

	/**
	 * 카테고리별 예제 조회
	 */
	List<SqlExample> findByCategory(String category);

	/**
	 * 최근 예제 조회
	 */
	@Query("SELECT s FROM SqlExample s ORDER BY s.createdAt DESC")
	List<SqlExample> findRecentExamples();

	/**
	 * 키워드로 예제 검색 (유사 쿼리 찾기)
	 */
	@Query("SELECT s FROM SqlExample s WHERE s.userQuery LIKE %:keyword% ORDER BY s.createdAt DESC")
	List<SqlExample> findByKeyword(@Param("keyword") String keyword);
}
