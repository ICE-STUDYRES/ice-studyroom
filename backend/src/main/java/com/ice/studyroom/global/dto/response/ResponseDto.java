package com.ice.studyroom.global.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.ice.studyroom.global.type.ResponseMessage;
import com.ice.studyroom.global.type.StatusCode;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResponseDto<T> {
	private String code;
	private String message;

	private T data;
	private List<FieldError> errors;
	private LocalDateTime timestamp;

	@Getter
	@Builder
	public static class FieldError {
		private String field; // 에러 필드명
		private String value; // 에러 값
		private String reason; // 에러 이유
	}

	// 성공 응답 (기본)
	public static <T> ResponseDto<T> of(T data) {
		return ResponseDto.<T>builder()
			.code(StatusCode.OK.getCode())
			.message(StatusCode.OK.getMessage())
			.data(data)
			.timestamp(LocalDateTime.now())
			.build();
	}

	// 성공 응답 (상수화된 응답 처리)
	public static <T> ResponseDto<T> success(ResponseMessage responseMessage) {
		return ResponseDto.<T>builder()
			.code(StatusCode.OK.getCode())
			.message(responseMessage.getMessage())
			.data(null)
			.timestamp(LocalDateTime.now())
			.build();
	}

	// 성공 응답 (메시지 포함)
	public static <T> ResponseDto<T> of(T data, String message) {
		return ResponseDto.<T>builder()
			.code(StatusCode.OK.getCode())
			.message(message)
			.data(data)
			.timestamp(LocalDateTime.now())
			.build();
	}

	// 성공 응답 (상태 코드 지정)
	public static <T> ResponseDto<T> of(StatusCode statusCode, T data) {
		return ResponseDto.<T>builder()
			.code(statusCode.getCode())
			.message(statusCode.getMessage())
			.data(data)
			.timestamp(LocalDateTime.now())
			.build();
	}

	// 에러 응답 (기본)
	public static <T> ResponseDto<T> error(StatusCode statusCode) {
		return ResponseDto.<T>builder()
			.code(statusCode.getCode())
			.message(statusCode.getMessage())
			.timestamp(LocalDateTime.now())
			.build();
	}

	// 에러 응답 (메시지 포함)
	public static <T> ResponseDto<T> error(StatusCode statusCode, String message) {
		return ResponseDto.<T>builder()
			.code(statusCode.getCode())
			.message(message)
			.timestamp(LocalDateTime.now())
			.build();
	}

	// 에러 응답 (필드 에러 포함)
	public static <T> ResponseDto<T> error(StatusCode statusCode, List<FieldError> errors) {
		return ResponseDto.<T>builder()
			.code(statusCode.getCode())
			.message(statusCode.getMessage())
			.errors(errors)
			.timestamp(LocalDateTime.now())
			.build();
	}
}
