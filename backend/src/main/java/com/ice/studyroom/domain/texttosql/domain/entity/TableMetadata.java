package com.ice.studyroom.domain.texttosql.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableMetadata implements Serializable {

	private String id;              // 예: "table:reservation"
	private String tableName;       // reservation
	private String description;     // 설명
	private String keywords;        // 키워드
	private float[] embedding;      // 벡터 (384차원)
	private List<String> columns;   // 컬럼 목록
	private List<String> relatedTables;  // 관련 테이블
}
