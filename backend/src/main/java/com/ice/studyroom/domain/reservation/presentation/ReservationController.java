package com.ice.studyroom.domain.reservation.presentation;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ice.studyroom.domain.reservation.application.ReservationService;
import com.ice.studyroom.domain.reservation.domain.entity.Reservation;
import com.ice.studyroom.domain.reservation.domain.entity.Schedule;
import com.ice.studyroom.domain.reservation.presentation.dto.request.CreateReservationRequest;
import com.ice.studyroom.domain.reservation.presentation.dto.request.DeleteReservationRequest;
import com.ice.studyroom.global.dto.response.ResponseDto;
import com.ice.studyroom.global.exception.BusinessException;
import com.ice.studyroom.global.type.StatusCode;

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

	/**
	 *
	 * @param authorizationHeader
	 * @return List 형태의 내 예약 정보들
	 * exception handler 전역 처리로 수정 예정
	 */
	@ExceptionHandler(BusinessException.class)
	@GetMapping("/reservations/my")
	public ResponseEntity<ResponseDto<List<Reservation>>> getMyReservation(
		@RequestHeader("Authorization") String authorizationHeader
	) {
		return ResponseEntity
			.status(StatusCode.OK.getStatus())
			.body(ResponseDto.of(reservationService.getMyReservation(authorizationHeader)));
	}

	@GetMapping("/schedules")
	public ResponseEntity<ResponseDto<List<Schedule>>> getSchedule() {
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(ResponseDto.of(reservationService.getSchedule()));
	}

	@Operation(summary = "스터디룸 예약", description = "스터디룸을 예약합니다.")
	@ApiResponse(responseCode = "201", description = "스터디룸 예약 성공")
	@PostMapping("/reservations")
	public ResponseEntity<ResponseDto<String>> createReservation(
		@RequestHeader("Authorization") String authorizationHeader,
		@Valid @RequestBody CreateReservationRequest request
	) {
		return ResponseEntity
			.status(StatusCode.OK.getStatus())
			.body(ResponseDto.of(reservationService.createReservation(authorizationHeader, request)));
	}

	// 예약 취소 시 본인 인증이 필요하다.
	@Operation(summary = "예약 취소", description = "예약을 취소합니다.")
	@DeleteMapping("/reservations/{id}")
	public ResponseEntity<ResponseDto<Void>> deleteReservation(@PathVariable Long id) {
		DeleteReservationRequest request = DeleteReservationRequest.builder()
			.reservationId(id)
			.userId(1L)  // 모킹했으니 무시해도 됩니다. 추후 회원가입 때 구현 예정
			.build();
		reservationService.cancelReservation(request);

		return ResponseEntity
			.status(StatusCode.OK.getStatus())
			.body(ResponseDto.<Void>of(null, "예약이 성공적으로 삭제되었습니다"));
	}
}
