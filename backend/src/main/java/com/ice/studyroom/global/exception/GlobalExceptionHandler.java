package com.ice.studyroom.global.exception;

import java.util.List;
import java.util.stream.Collectors;

import com.ice.studyroom.domain.membership.domain.exception.member.MemberNotFoundException;
import com.ice.studyroom.domain.membership.domain.exception.member.MemberPenaltyException;
import com.ice.studyroom.domain.reservation.application.exception.ParticipantAlreadyReservedException;
import com.ice.studyroom.domain.reservation.domain.exception.reservation.ReservationNotFoundException;
import com.ice.studyroom.domain.reservation.domain.exception.reservation.InvalidCancelAttemptException;
import com.ice.studyroom.domain.reservation.domain.exception.reservation.qr.InvalidEntranceAttemptException;
import com.ice.studyroom.domain.reservation.domain.exception.reservation.qr.InvalidEntranceTimeException;
import com.ice.studyroom.domain.reservation.domain.exception.reservation.qr.QrIssuanceNotAllowedException;
import com.ice.studyroom.domain.reservation.domain.exception.reservation.ReservationAccessDeniedException;
import com.ice.studyroom.domain.reservation.domain.exception.reservation.ReservationScheduleNotFoundException;
import com.ice.studyroom.domain.reservation.util.ReservationLogUtil;
import com.ice.studyroom.domain.schedule.domain.exception.schedule.ScheduleNotFoundException;
import com.ice.studyroom.global.exception.token.InvalidQrTokenException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.ice.studyroom.global.dto.response.ResponseDto;
import com.ice.studyroom.global.type.StatusCode;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	// JSON 파싱/타입 변환 실패 시
	@ExceptionHandler(HttpMessageNotReadableException.class)
	protected ResponseEntity<ResponseDto<Object>> handleHttpMessageNotReadable(
		HttpMessageNotReadableException ex) {

		// 상세 에러는 로그로만 남김
		log.error("Message not readable exception occurred", ex);

		// 클라이언트에는 최소한의 정보만 전달
		return ResponseEntity
			.status(StatusCode.BAD_REQUEST.getStatus())
			.body(ResponseDto.error(
				StatusCode.BAD_REQUEST,
				"Invalid request format"  // 일반적인 메시지만 전달
			));
	}

	// Bean Validation 예외 처리도 수정
	@ExceptionHandler(MethodArgumentNotValidException.class)
	protected ResponseEntity<ResponseDto<Object>> handleMethodArgumentNotValid(
		MethodArgumentNotValidException ex) {
		// 로그에는 상세 정보 포함
		log.error("Validation error occurred: {}", ex.getBindingResult());

		// 필드 에러는 포함하되, 최소한의 정보만
		List<ResponseDto.FieldError> errors = ex.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(error -> ResponseDto.FieldError.builder()
				.field(error.getField())
				// 거부된 값은 로깅만 하고 응답에는 포함하지 않음
				//.value(String.valueOf(error.getRejectedValue()))
				.reason(error.getDefaultMessage())
				.build())
			.collect(Collectors.toList());

		return ResponseEntity
			.status(StatusCode.INVALID_INPUT.getStatus())
			.body(ResponseDto.error(StatusCode.INVALID_INPUT, errors));
	}

	@ExceptionHandler(InvalidCancelAttemptException.class)
	public ResponseEntity<ResponseDto<Object>> handleInvalidCancelAttempt(InvalidCancelAttemptException ex) {
		ReservationLogUtil.logWarn("예약 취소 실패 - 입실 시간 초과", "예약 ID: " + ex.getReservationId());
		return ResponseEntity
			.status(ex.getStatusCode().getStatus())
			.body(ResponseDto.error(ex.getStatusCode(), ex.getMessage()));
	}

	@ExceptionHandler(QrIssuanceNotAllowedException.class)
	public ResponseEntity<ResponseDto<Object>> handleQrIssuanceNotAllowed(QrIssuanceNotAllowedException ex) {
		ReservationLogUtil.logWarn("QR코드 요청 실패 - 예약 상태 아님", "예약 ID: " + "예약 ID: " + ex.getReservationId());
		return ResponseEntity
			.status(ex.getStatusCode().getStatus())
			.body(ResponseDto.error(ex.getStatusCode(), ex.getMessage()));
	}

	@ExceptionHandler(ReservationScheduleNotFoundException.class)
	public ResponseEntity<ResponseDto<Object>> handleReservationScheduleNotFound(ReservationScheduleNotFoundException ex) {
		ReservationLogUtil.logWarn("["+ ex.getDescription() +"]" + "찾을 수 없는 예약", "예약 ID: " + ex.getReservationId() + "유효하지않는 스케줄 ID " + ex.getScheduleId());
		return ResponseEntity
			.status(ex.getStatusCode().getStatus())
			.body(ResponseDto.error(ex.getStatusCode(), ex.getMessage()));
	}

	@ExceptionHandler(ReservationNotFoundException.class)
	public ResponseEntity<ResponseDto<Object>> handleReservationNotFound(ReservationNotFoundException ex) {
		ReservationLogUtil.logWarn("["+ ex.getDescription() +"]" + "찾을 수 없는 예약", "예약 ID: " + ex.getReservationId() + " 접근 시도자: " + ex.getRequesterEmail());
		return ResponseEntity
			.status(ex.getStatusCode().getStatus())
			.body(ResponseDto.error(ex.getStatusCode(), ex.getMessage()));
	}

	@ExceptionHandler(ScheduleNotFoundException.class)
	public ResponseEntity<ResponseDto<Object>> handleScheduleNotFound(ScheduleNotFoundException ex) {
		// 로그 메시지 동적 구성 로직
		String scheduleInfo = buildScheduleInfo(ex);
		String requesterInfo = ex.getRequesterEmail() != null ?
			" 접근 시도자: " + ex.getRequesterEmail() :
			" 접근 시도자: Unknown";

		ReservationLogUtil.logWarn(
			"[" + ex.getDescription() + "] 찾을 수 없는 스케줄",
			scheduleInfo + requesterInfo
		);

		return ResponseEntity
			.status(ex.getStatusCode().getStatus())
			.body(ResponseDto.error(ex.getStatusCode(), ex.getMessage()));
	}

	private String buildScheduleInfo(ScheduleNotFoundException ex) {
		if (ex.getScheduleIds() != null) {
			return "스케줄 IDs: " + ex.getScheduleIds();
		} else if (ex.getScheduleId() != null) {
			return "스케줄 ID: " + ex.getScheduleId();
		} else {
			return "스케줄 ID: Unknown";
		}
	}

	@ExceptionHandler(MemberNotFoundException.class)
	public ResponseEntity<ResponseDto<Object>> handleMemberNotFound(MemberNotFoundException ex) {
		ReservationLogUtil.logWarn("["+ ex.getDescription() +"]" + "찾을 수 없는 사용자", " 접근 시도자: " + ex.getRequesterEmail());
		return ResponseEntity
			.status(ex.getStatusCode().getStatus())
			.body(ResponseDto.error(ex.getStatusCode(), ex.getMessage()));
	}

	@ExceptionHandler(ReservationAccessDeniedException.class)
	public ResponseEntity<ResponseDto<Object>> handleReservationAccessDenied(ReservationAccessDeniedException ex) {
		ReservationLogUtil.logWarn("["+ ex.getDescription() +"]" + "예약 접근 권한 없음", "예약 ID: " + ex.getReservationId() + " 접근 시도자: " + ex.getRequesterEmail());
		return ResponseEntity
			.status(ex.getStatusCode().getStatus())
			.body(ResponseDto.error(ex.getStatusCode(), ex.getMessage()));
	}

	@ExceptionHandler(InvalidEntranceAttemptException.class)
	public ResponseEntity<ResponseDto<Object>> handleInvalidEntranceAttempt(InvalidEntranceAttemptException ex) {
		ReservationLogUtil.logWarn("QR 입장 실패 - 사전 검증 오류 " + ex.getMessage(), " 예약 ID: " + ex.getReservationId());
		return ResponseEntity
			.status(ex.getStatusCode().getStatus())
			.body(ResponseDto.error(ex.getStatusCode(), ex.getMessage()));
	}

	@ExceptionHandler(InvalidEntranceTimeException.class)
	public ResponseEntity<ResponseDto<Object>> handleInvalidEntranceTime(InvalidEntranceTimeException ex) {
		ReservationLogUtil.logWarn("QR 입장 실패 - 입실 시간 오류 " + ex.getMessage(), " 예약 ID: " + ex.getReservationId());
		return ResponseEntity
			.status(ex.getStatusCode().getStatus())
			.body(ResponseDto.error(ex.getStatusCode(), ex.getMessage()));
	}

	@ExceptionHandler(InvalidQrTokenException.class)
	public ResponseEntity<ResponseDto<Object>> handleInvalidQrToken(InvalidQrTokenException ex) {
		log.error("토큰 조회 실패 예외 발생: {}", ex.getMessage());
		return ResponseEntity
			.status(ex.getStatusCode().getStatus())
			.body(ResponseDto.error(ex.getStatusCode(), ex.getMessage()));
	}

	@ExceptionHandler(ParticipantAlreadyReservedException.class)
	public ResponseEntity<ResponseDto<Object>> handleParticipantAlreadyReserved(ParticipantAlreadyReservedException ex) {
		ReservationLogUtil.logWarn("단체 예약 실패 - 중복된 참여자 이메일", "이메일: " + ex.getEmail());
		return ResponseEntity
			.status(ex.getStatusCode().getStatus())
			.body(ResponseDto.error(ex.getStatusCode(), ex.getMessage()));
	}

	@ExceptionHandler(MemberPenaltyException.class)
	public ResponseEntity<ResponseDto<Object>> handleMemberPenalty(MemberPenaltyException ex) {
		ReservationLogUtil.logWarn("단체 예약 실패 - 패널티 상태의 참여자 존재", "이메일: " + ex.getEmail());
		return ResponseEntity
			.status(ex.getStatusCode().getStatus())
			.body(ResponseDto.error(ex.getStatusCode(), ex.getMessage()));
	}

	// 비즈니스 예외 처리
	@ExceptionHandler(BusinessException.class)
	protected ResponseEntity<ResponseDto<Object>> handleBusinessException(BusinessException ex) {
		log.error("BusinessException caught in GlobalExceptionHandler: {}", ex.getMessage());
		return ResponseEntity
			.status(ex.getStatusCode().getStatus())
			.body(ResponseDto.error(ex.getStatusCode(), ex.getMessage()));
	}

	// 메세지 컨버팅 실패 예외 처리
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	protected ResponseEntity<ResponseDto<Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
		log.warn("TypeMismatchException caught: {}", ex.getMessage());
		StatusCode status = StatusCode.BAD_REQUEST;
		String message = String.format("DTO 파라미터 값 '%s'는 허용되지 않는 값입니다.", ex.getValue());
		return ResponseEntity
			.status(status.getStatus())
			.body(ResponseDto.error(status, message));
	}

	// 일반 예외 처리
	@ExceptionHandler(Exception.class)
	protected ResponseEntity<ResponseDto<Object>> handleException(Exception ex) {
		log.error("Unhandled exception occurred", ex);
		return ResponseEntity
			.status(StatusCode.INTERNAL_ERROR.getStatus())
			.body(ResponseDto.error(StatusCode.INTERNAL_ERROR, ex.getMessage()));
	}
}
