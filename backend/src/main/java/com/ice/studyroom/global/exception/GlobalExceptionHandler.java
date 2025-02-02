package com.ice.studyroom.global.exception;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.ice.studyroom.global.dto.response.ResponseDto;
import com.ice.studyroom.global.type.StatusCode;

import lombok.extern.slf4j.Slf4j;

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

	@ExceptionHandler(AttendanceException.class)
	public ResponseEntity<String> handleAttendanceException(AttendanceException ex) {
		return ResponseEntity.status(ex.getStatus()).body(ex.getMessage());
	}

	// 비즈니스 예외 처리
	@ExceptionHandler(BusinessException.class)
	protected ResponseEntity<ResponseDto<Object>> handleBusinessException(BusinessException ex) {
		return ResponseEntity
			.status(ex.getStatusCode().getStatus())
			.body(ResponseDto.error(ex.getStatusCode(), ex.getMessage()));
	}

	// 일반 예외 처리
	@ExceptionHandler(Exception.class)
	protected ResponseEntity<ResponseDto<Object>> handleException(Exception ex) {
		log.error("Unhandled exception occurred", ex);
		return ResponseEntity
			.status(StatusCode.INTERNAL_ERROR.getStatus())
			.body(ResponseDto.error(StatusCode.INTERNAL_ERROR));
	}
}
