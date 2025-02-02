package com.ice.studyroom.domain.reservation.presentation;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ice.studyroom.domain.reservation.application.ReservationService;
import com.ice.studyroom.domain.reservation.domain.entity.Reservation;
import com.ice.studyroom.domain.reservation.domain.type.ReservationStatus;
import com.ice.studyroom.domain.reservation.presentation.dto.request.QrEntranceRequest;
import com.ice.studyroom.global.dto.response.ResponseDto;
import com.ice.studyroom.global.type.StatusCode;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/qr")
@RequiredArgsConstructor
public class QrController {
	private final ReservationService reservationService;

	@Operation(summary = "예약 QR 코드 인식", description = "QR 코드를 이용하여 입실을 시도합니다.")
	@ApiResponse(responseCode = "200", description = "정상 출석 혹은 지각")
	@ApiResponse(responseCode = "403", description = "예약한 스터디룸 시작 시간 이전")
	@ApiResponse(responseCode = "401", description = "예약한 스터디룸 종료 시간 이후")
	@PostMapping("/recognize")
	public ResponseEntity<ResponseDto<ReservationStatus>> qrEntrance(
		@Valid @RequestBody QrEntranceRequest request) {
		return ResponseEntity
			.status(StatusCode.OK.getStatus())
			.body(ResponseDto.of(reservationService.qrEntrance(request)));
	}
}
