package com.ice.studyroom.domain.texttosql.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

@Schema(description = "쿼리 응답 DTO")
public record QueryResponse(

	@Schema(description = "성공 여부")
	@JsonProperty("success")
	boolean success,

	@Schema(description = "생성된 SQL")
	@JsonProperty("sql")
	String sql,

	@Schema(description = "조회 결과")
	@JsonProperty("data")
	List<Map<String, Object>> data,

	@Schema(description = "결과 행 수")
	@JsonProperty("count")
	int count,

	@Schema(description = "실행 시간 (ms)")
	@JsonProperty("executionTimeMs")
	Long executionTimeMs,

	@Schema(description = "시도 횟수")
	@JsonProperty("attempts")
	int attempts,

	@Schema(description = "캐시 여부")
	@JsonProperty("cached")
	boolean cached
) {
	public static QueryResponse success(String sql, List<Map<String, Object>> data, Long executionTimeMs, int attempts) {
		return new QueryResponse(true, sql, data, data.size(), executionTimeMs, attempts, attempts == 0);
	}
}
