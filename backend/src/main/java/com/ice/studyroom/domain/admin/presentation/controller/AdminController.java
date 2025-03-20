package com.ice.studyroom.domain.admin.presentation.controller;


import com.ice.studyroom.domain.admin.application.AdminService;
import com.ice.studyroom.domain.admin.domain.type.DayOfWeekStatus;
import com.ice.studyroom.domain.admin.presentation.dto.request.AdminDelPenaltyRequest;
import com.ice.studyroom.domain.admin.presentation.dto.request.AdminOccupyRequest;
import com.ice.studyroom.domain.admin.presentation.dto.request.AdminSetPenaltyRequest;
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
		summary = "요청한 요일에 따른 방 정보 반환",
		description = "관리자가 특정 요일에 대한 방 정보를 요청합니다."
	)
	@ApiResponse(responseCode = "200", description = "방 상태 반환 성공")
	@ApiResponse(responseCode = "500", description = "방 상태 반환 실패")
	@GetMapping("/room-time-slots")
	public ResponseEntity<ResponseDto<List<RoomScheduleInfoDto>>> adminGetRooms(
		@RequestParam DayOfWeekStatus dayOfWeek
	) {
		List<RoomScheduleInfoDto> rooms = adminService.getRoomByDayOfWeek(dayOfWeek);
		return ResponseEntity.status(HttpStatus.OK).body(ResponseDto.of(rooms, "요청한 요일의 방 정보가 성공적으로 반환되었습니다."));
	}

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
			.body(ResponseDto.of(reservedRooms));
	}

	@Operation(
		summary = "패널티 유저 목록 반환",
		description = "전체 패널티 기록을 반환합니다."
	)
	@ApiResponse(responseCode = "200", description = "패널티 유저 목록 반환 성공")
	@ApiResponse(responseCode = "500", description = "패널티 유저 목록 반환 실패")
	@GetMapping("/penalty")
	public ResponseEntity<ResponseDto<List<AdminPenaltyRecordResponse>>> adminGetPenaltyRecords() {
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(ResponseDto.of(adminService.adminGetPenaltyRecords()));
	}

	@Operation(
		summary = "패널티 부여",
		description = "지정한 유저에게 패널티를 부여합니다."
	)
	@ApiResponse(responseCode = "200", description = "패널티 부여 성공")
	@ApiResponse(responseCode = "500", description = "패널티 부여 실패")
	@PostMapping("/penalty")
	public ResponseEntity<ResponseDto<String>> adminAddPenalty(
		@Valid @RequestBody AdminSetPenaltyRequest request
	) {
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(ResponseDto.of(adminService.adminSetPenalty(request)));
	}

	@Operation(
		summary = "패널티 해제",
		description = "지정한 유저의 패널티를 해제합니다."
	)
	@ApiResponse(responseCode = "200", description = "패널티 해제 성공")
	@ApiResponse(responseCode = "500", description = "패널티 해제 실패")
	@DeleteMapping("/penalty")
	public ResponseEntity<ResponseDto<String>> adminDeletePenalty(
		@Valid @RequestBody AdminDelPenaltyRequest request
	) {
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(ResponseDto.of(adminService.adminDelPenalty(request)));
	}
}
