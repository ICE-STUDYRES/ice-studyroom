package com.ice.studyroom.domain.admin.presentation.controller;


import com.ice.studyroom.domain.admin.application.AdminService;
import com.ice.studyroom.domain.admin.presentation.dto.request.AdminCreateOccupyRequest;
import com.ice.studyroom.domain.admin.presentation.dto.request.AdminPenaltyRequest;
import com.ice.studyroom.domain.admin.presentation.dto.response.AdminCreateOccupyResponse;
import com.ice.studyroom.domain.admin.presentation.dto.response.AdminDeleteOccupyResponse;
import com.ice.studyroom.domain.admin.presentation.dto.response.AdminPenaltyControlResponse;
import com.ice.studyroom.domain.admin.presentation.dto.response.AdminPenaltyRecordResponse;
import com.ice.studyroom.global.dto.response.ResponseDto;
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

	@PostMapping("/room-time-slots/occupy")
	public ResponseEntity<ResponseDto<AdminCreateOccupyResponse>> adminOccupySchedule(
			@Valid @RequestBody AdminCreateOccupyRequest request
	) {
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(ResponseDto.of(adminService.adminOccupyRoom(request)));
	}

	//예약된 방 ID 확인
	@GetMapping("/room-time-slots/occupy")
	public ResponseEntity<ResponseDto<List<Long>>> getReservedRooms() {
		List<Long> reservedRooms = adminService.getReservedRoomIds();
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(ResponseDto.of(reservedRooms, "예약된 방들의 id가 성공적으로 반환되었습니다."));
	}

	//특정 방의 상태를 AVAILABLE로 변경
	@DeleteMapping("/room-time-slots/occupy")
	public ResponseEntity<ResponseDto<AdminDeleteOccupyResponse>> adminFreeOccupy(
		@Valid @RequestBody AdminCreateOccupyRequest request
	){
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(ResponseDto.of(adminService.adminDeleteOccupy(request)));
	}

	@GetMapping("/penalty/reasons")
	public ResponseEntity<ResponseDto<List<AdminPenaltyRecordResponse>>> adminGetPenaltyRecords(
		@Valid @RequestBody AdminPenaltyRequest request
	) {
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(ResponseDto.of(adminService.adminGetPenaltyRecords(request), "성공적으로 패널티 이유 목록을 반환했습니다."));
	}

	@PutMapping("/penalty/add")
	public ResponseEntity<ResponseDto<AdminPenaltyControlResponse>> adminAddPenalties(
		@Valid @RequestBody AdminPenaltyRequest request
	) {
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(ResponseDto.of(adminService.adminAddPenalty(request), "관리자가 성공적으로 패널티 횟수를 증가시켰습니다."));
	}

	@PutMapping("/penalty/minus")
	public ResponseEntity<ResponseDto<AdminPenaltyControlResponse>> adminMinusPenalties(
		@Valid @RequestBody AdminPenaltyRequest request
	) {
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(ResponseDto.of(adminService.adminSubtractPenalty(request), "관리자가 성공적으로 패널티 횟수를 차감시켰습니다."));
	}
}
