package com.ice.studyroom.domain.ranking.presentation.controller;

import com.ice.studyroom.domain.ranking.application.query.RankingQueryService;
import com.ice.studyroom.domain.ranking.domain.type.RankingPeriod;
import com.ice.studyroom.domain.ranking.presentation.dto.response.RankingResponse;
import com.ice.studyroom.global.dto.response.ResponseDto;
import com.ice.studyroom.global.type.StatusCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class RankingQueryController {

	private final RankingQueryService rankingQueryService;

	@Operation(
		summary = "기간별 랭킹 Top 5 조회",
		description = " 월간(MONTHLY), 학기(SEMESTER), 연간(YEARLY) 기준 Top 5 랭킹을 조회합니다. 주간(WEEKLY)은 WebSocket을 통해 실시간 제공되므로 REST 조회 대상이 아닙니다."
	)
	@ApiResponse(responseCode = "200", description = "랭킹 조회 성공")
	@ApiResponse(responseCode = "400", description = "잘못된 period 값 요청")
	@ApiResponse(responseCode = "500", description = "서버 내부 오류")
	@GetMapping("/rankings")
	public ResponseEntity<ResponseDto<List<RankingResponse>>> getRankings(

		@Parameter(
			description = "조회할 랭킹 기간",
			required = true,
			schema = @Schema(allowableValues = {"MONTHLY", "SEMESTER", "YEARLY"}
			)
		)
		@RequestParam RankingPeriod period
	) {

		List<RankingResponse> responses =
			rankingQueryService.getTop5(period);

		return ResponseEntity
			.status(StatusCode.OK.getStatus())
			.body(ResponseDto.of(responses));
	}
}
