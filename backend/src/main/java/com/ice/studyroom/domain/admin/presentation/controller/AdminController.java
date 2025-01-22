package com.ice.studyroom.domain.admin.presentation.controller;


import com.ice.studyroom.domain.admin.application.AdminService;
import com.ice.studyroom.domain.admin.presentation.dto.request.AdminCreateOccupyRequest;
import com.ice.studyroom.domain.admin.presentation.dto.response.AdminCreateOccupyResponse;
import com.ice.studyroom.global.dto.response.ResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

	private final AdminService adminService;

	@PostMapping("/preOccupy")
	public ResponseEntity<ResponseDto<AdminCreateOccupyResponse>> adminOccupySchedule(
		@Valid @RequestBody AdminCreateOccupyRequest request
	) {
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(ResponseDto.of(adminService.adminOccupyRoom(request)));

		/*
		이후 버전 생각한거니 무시해주세요.
		 */
//		AdminCreateReserveResponse response = adminService.adminReserveRoom(request);
//		// 상태 코드를 동적으로 설정
//		HttpStatus status = response.message().equals("이미 예약되어 있는 스케줄입니다.")
//			? HttpStatus.BAD_REQUEST
//			: HttpStatus.OK;
//
//		return ResponseEntity
//			.status(status)
//			.body(ResponseDto.of(response, status == HttpStatus.OK ? "OK" : "Bad Request"));
	}
}
