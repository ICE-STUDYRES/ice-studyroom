package com.ice.studyroom.domain.reservation.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ice.studyroom.domain.reservation.application.ReservationService;
import com.ice.studyroom.domain.reservation.dto.request.CreateReservationRequest;
import com.ice.studyroom.domain.reservation.dto.request.DeleteReservationRequest;
import com.ice.studyroom.domain.reservation.dto.response.ReservationResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@Tag(name = "Study Room", description = "스터디룸 API")
@RequiredArgsConstructor
public class ReservationController {

	private final ReservationService reservationService;

	@Operation(summary = "스터디룸 예약", description = "스터디룸을 예약합니다.")
	@ApiResponse(responseCode = "201", description = "스터디룸 예약 성공")
	@PostMapping("/reservations")
	public ResponseEntity<ReservationResponse> createReservation(
		@Valid @RequestBody CreateReservationRequest request
	) {
		return ResponseEntity
			.status(HttpStatus.CREATED)
			.body(reservationService.createReservation(request));
	}

	@Operation(summary = "예약 삭제", description = "예약을 삭제합니다.")
	@DeleteMapping("/reservations/{id}")
	public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
		DeleteReservationRequest request = DeleteReservationRequest.builder()
			.reservationId(id)
			.userId(1L)  // 모킹했으니 무시해도 됩니다. 추후 회원가입 때 구현 예정
			.build();
		reservationService.cancelReservation(request);
		return ResponseEntity.ok().build();
	}
}
