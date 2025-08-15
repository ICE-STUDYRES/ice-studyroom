package com.ice.studyroom.domain.reservation.presentation;

import java.util.List;
import java.util.Optional;

import com.ice.studyroom.global.type.ResponseMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ice.studyroom.domain.reservation.application.ReservationService;
import com.ice.studyroom.domain.reservation.application.QrEntranceApplicationService;
import com.ice.studyroom.domain.reservation.presentation.dto.request.CreateReservationRequest;
import com.ice.studyroom.domain.reservation.presentation.dto.response.CancelReservationResponse;
import com.ice.studyroom.domain.reservation.presentation.dto.response.GetMostRecentReservationResponse;
import com.ice.studyroom.domain.reservation.presentation.dto.response.GetReservationsResponse;
import com.ice.studyroom.global.dto.response.ResponseDto;
import com.ice.studyroom.global.type.StatusCode;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reservations")
@Tag(
	name = "Study Room",
	description = "스터디룸 예약 및 관리 API. 예약 생성, 조회, 취소 등의 기능을 제공합니다."
)
@RequiredArgsConstructor
public class ReservationController {

	private final ReservationService reservationService;
	private final QrEntranceApplicationService qrEntranceApplicationService;

	/**
	 *
	 * @param authorizationHeader
	 * @return List 형태의 내 예약 정보들
	 * exception handler 전역 처리로 수정 예정
	 */
	@Operation(summary = "내 예약 정보 조회", description = "현재 사용자의 예약 정보를 조회합니다.")
	@ApiResponse(responseCode = "200", description = "예약 정보 조회 성공")
	@ApiResponse(responseCode = "500", description = "예약 정보 조회 실패")
	@GetMapping("/my")
	public ResponseEntity<ResponseDto<List<GetReservationsResponse>>> getMyReservation(
		@RequestHeader("Authorization") String authorizationHeader
	) {
		return ResponseEntity
			.status(StatusCode.OK.getStatus())
			.body(ResponseDto.of(reservationService.getReservations(authorizationHeader)));
	}

	@GetMapping("/my/latest")
	public ResponseEntity<ResponseDto<GetMostRecentReservationResponse>> getMyMostRecentReservation(
		@RequestHeader("Authorization") String authorizationHeader
	) {
		Optional<GetMostRecentReservationResponse> reservation = reservationService.getMyMostRecentReservation(
			authorizationHeader);

		return ResponseEntity.ok(ResponseDto.of(
			reservation.orElse(null),
			reservation.isPresent() ? "예약 조회 성공" : "최근 예약 내역이 없습니다."
		));
	}

	@Operation(summary = "예약 QR 코드 조회", description = "특정 예약의 QR 코드를 조회합니다.")
	@ApiResponse(responseCode = "200", description = "QR 코드 조회 성공")
	@ApiResponse(responseCode = "500", description = "QR 코드 조회 실패")
	@GetMapping("/my/{resId}")
	public ResponseEntity<ResponseDto<String>> getMyReservationQrCode(
		@PathVariable Long resId,
		@RequestHeader("Authorization") String authorizationHeader
	) {
		return ResponseEntity
			.status(StatusCode.OK.getStatus())
			.body(ResponseDto.of(qrEntranceApplicationService.getMyReservationQrCode(resId, authorizationHeader)));
	}

	@Operation(summary = "단체 스터디룸 예약", description = "단체 단위로 스터디룸을 예약합니다.")
	@ApiResponse(responseCode = "201", description = "스터디룸 단체 예약 성공")
	@ApiResponse(responseCode = "500", description = "스터디룸 단체 예약 실패")
	@PostMapping("/group")
	public ResponseEntity<ResponseDto<Void>> reserveGroup(
		@RequestHeader("Authorization") String authorizationHeader,
		@Valid @RequestBody CreateReservationRequest request
	) {
		reservationService.createGroupReservation(authorizationHeader, request);
		return ResponseEntity
			.status(StatusCode.OK.getStatus())
			.body(ResponseDto.success(ResponseMessage.GROUP_RESERVATION_SUCCESS));
	}

	// 예약 취소 시 본인 인증이 필요하다.
	@Operation(summary = "개인 스터디룸 예약", description = "개인 단위로 스터디룸을 예약합니다.")
	@ApiResponse(responseCode = "200", description = "스터디룸 개인 예약 성공")
	@ApiResponse(responseCode = "500", description = "스터디룸 개인 예약 실패")
	@PostMapping("/individual")
	public ResponseEntity<ResponseDto<Void>> reserveIndividual(
		@RequestHeader("Authorization") String authorizationHeader,
		@Valid @RequestBody CreateReservationRequest request
	) {
		reservationService.createIndividualReservation(authorizationHeader, request);
		return ResponseEntity
			.status(StatusCode.OK.getStatus())
			.body(ResponseDto.success(ResponseMessage.INDIVIDUAL_RESERVATION_SUCCESS));
	}

	@Operation(summary = "예약 취소", description = "예약을 취소합니다.")
	@ApiResponse(responseCode = "200", description = "예약 취소 성공")
	@ApiResponse(responseCode = "500", description = "예약 취소 실패")
	@DeleteMapping("/{id}")
	public ResponseEntity<ResponseDto<CancelReservationResponse>> cancelReservation(
		@PathVariable Long id,
		@RequestHeader("Authorization") String authorizationHeader
	) {
		return ResponseEntity
			.status(StatusCode.OK.getStatus())
			.body(ResponseDto.of(reservationService.cancelReservation(id, authorizationHeader)));
	}

	@Operation(summary = "예약 연장", description = "예약을 연장합니다.")
	@ApiResponse(responseCode = "200", description = "예약 연장 성공")
	@ApiResponse(responseCode = "500", description = "예약 연장 실패")
	@PatchMapping("/{id}")
	public ResponseEntity<ResponseDto<String>> extendReservation(
		@PathVariable Long id,
		@RequestHeader("Authorization") String authorizationHeader
	) {
		return ResponseEntity
			.status(StatusCode.OK.getStatus())
			.body(ResponseDto.of(reservationService.extendReservation(id, authorizationHeader)));
	}
}
