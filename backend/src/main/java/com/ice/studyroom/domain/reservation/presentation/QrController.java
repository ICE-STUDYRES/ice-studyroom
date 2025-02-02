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

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/qr")
@RequiredArgsConstructor
public class QrController {
	private final ReservationService reservationService;
	@PostMapping("/recognize")
	public ResponseEntity<ResponseDto<ReservationStatus>> qrEntrance(
		@Valid @RequestBody QrEntranceRequest request) {
		return ResponseEntity
			.status(StatusCode.OK.getStatus())
			.body(ResponseDto.of(reservationService.qrEntrance(request)));
	}
}
