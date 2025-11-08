package com.ice.studyroom.domain.texttosql.presentation.controller;

import com.ice.studyroom.domain.texttosql.application.TextToSqlService;
import com.ice.studyroom.domain.texttosql.presentation.dto.request.QueryRequest;
import com.ice.studyroom.domain.texttosql.presentation.dto.response.QueryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "Text-to-SQL", description = "자연어 쿼리 API")
@RestController
@RequestMapping("/api/query")
@RequiredArgsConstructor
public class TextToSqlController {

	private final TextToSqlService textToSqlService;

	@Operation(
		summary = "자연어 쿼리 실행",
		description = "자연어 질문을 SQL로 변환하여 실행합니다. ADMIN 권한 필요."
	)
	@PostMapping
	public ResponseEntity<QueryResponse> executeQuery(
		@Valid @RequestBody QueryRequest request,
		@RequestHeader("Authorization") String token
	) {
		log.info("쿼리 요청: {}", request.query());

		TextToSqlService.QueryResult result = textToSqlService.executeQuery(request.query());

		return ResponseEntity.ok(
			QueryResponse.success(
				result.sql(),
				result.data(),
				result.executionTimeMs(),
				result.attempts()
			)
		);
	}
}
