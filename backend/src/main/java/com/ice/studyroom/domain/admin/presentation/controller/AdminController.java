package com.ice.studyroom.domain.admin.presentation.controller;


import com.ice.studyroom.domain.admin.application.AdminService;
import com.ice.studyroom.domain.admin.presentation.dto.request.AdminOccupyRequest;
import com.ice.studyroom.domain.admin.presentation.dto.request.AdminPenaltyRequest;
import com.ice.studyroom.domain.admin.presentation.dto.response.*;
import com.ice.studyroom.global.dto.response.ResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

	private final AdminService adminService;

	@Operation(
		summary = "방 시간대를 점유 상태로 설정",
		description = "관리자가 특정 스터디룸의 특정 시간대를 점유 상태(예약 불가)로 설정합니다."
	)
	@ApiResponse(responseCode = "200", description = "방 선점 상태 변경 성공")
	@ApiResponse(responseCode = "500", description = "방 선점 상태 변경 실패")
	@PostMapping("/room-time-slots/occupy")
	public ResponseEntity<ResponseDto<AdminOccupyResponse>> adminOccupySchedule(
		@Valid @RequestBody AdminOccupyRequest request
	) {
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(ResponseDto.of(adminService.adminOccupyRooms(request)));
	}

	@Operation(
		summary = "예약된 방 ID 조회",
		description = "현재 예약된 스터디룸의 ID 목록을 반환합니다."
	)
	@ApiResponse(responseCode = "200", description = "예약된 방 ID 조회 성공")
	@ApiResponse(responseCode = "500", description = "예약된 방 ID 조회 실패")
	@GetMapping("/room-time-slots/occupy")
	public ResponseEntity<ResponseDto<List<AdminGetReservedResponse>>> getReservedRooms() {
		List<AdminGetReservedResponse> reservedRooms = adminService.getReservedRoomIds();
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(ResponseDto.of(reservedRooms, "예약된 방들의 id가 성공적으로 반환되었습니다."));
	}

	@GetMapping("/penalty/reasons")
	public ResponseEntity<ResponseDto<List<AdminPenaltyRecordResponse>>> adminGetPenaltyRecords(
		@Valid @RequestBody AdminPenaltyRequest request
	) {
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(ResponseDto.of(adminService.adminGetPenaltyRecords(request), "성공적으로 패널티 이유 목록을 반환했습니다."));
	}

	@PostMapping("/penalty")
	public ResponseEntity<ResponseDto<AdminPenaltyControlResponse>> adminAddPenalties(
		@Valid @RequestBody AdminPenaltyRequest request
	) {
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(ResponseDto.of(adminService.adminSetPenalty(request)));
	}
}
