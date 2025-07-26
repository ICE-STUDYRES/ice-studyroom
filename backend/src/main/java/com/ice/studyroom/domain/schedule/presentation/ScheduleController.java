package com.ice.studyroom.domain.schedule.presentation;

import com.ice.studyroom.domain.schedule.domain.entity.Schedule;
import com.ice.studyroom.domain.schedule.application.ScheduleService;
import com.ice.studyroom.global.dto.response.ResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@Tag(
	name = "Study Room",
	description = "스터디룸 스케줄 조회, 빈자리 알림 기능을 제공합니다."
)
@RequiredArgsConstructor
public class ScheduleController {

	private final ScheduleService scheduleService;

	@Operation(summary = "스터디룸 일정 조회", description = "스터디룸 예약 가능한 일정을 조회합니다.")
	@ApiResponse(responseCode = "200", description = "스터디룸 일정 조회 성공")
	@ApiResponse(responseCode = "500", description = "스터디룸 일정 조회 실패")
	@GetMapping()
	public ResponseEntity<ResponseDto<List<Schedule>>> getSchedule() {
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(ResponseDto.of(scheduleService.getSchedule()));
	}

	@Operation(summary = "특정 스케줄 빈자리 알림 신청", description = "이미 예약된 특정 스케줄에 빈자리가 생길 경우 알림을 받도록 등록합니다.")
	@PostMapping("/{scheduleId}/vacancy-alert")
	public ResponseEntity<ResponseDto<String>> registerVacancyAlert(
		@PathVariable long scheduleId,
		@RequestHeader("Authorization") String authorizationHeader
	) {
		return ResponseEntity.ok(ResponseDto.of(scheduleService.registerVacancyAlert(scheduleId, authorizationHeader)));
	}
}
