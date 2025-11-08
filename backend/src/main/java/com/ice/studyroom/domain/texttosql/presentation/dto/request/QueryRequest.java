package com.ice.studyroom.domain.texttosql.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "자연어 쿼리 요청 DTO")
public record QueryRequest(

	@NotBlank(message = "질문을 입력해주세요")
	@Size(max = 500, message = "질문은 500자 이내로 입력해주세요")
	@Schema(description = "자연어 질문", example = "오늘 예약된 스터디룸 목록을 보여줘")
	String query
) {}
