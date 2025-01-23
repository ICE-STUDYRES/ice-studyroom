package com.ice.studyroom.global.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StatusCode {
	// Success
	OK(200, "S200", "Success"),
	CREATED(201, "S201", "Created Success"),

	// Client Errors
	BAD_REQUEST(400, "C400", "Bad Request"),
	INVALID_VERIFICATION_CODE(400, "C400", "유효하지 않은 인증코드입니다."),
	UNAUTHORIZED(401, "C401", "Unauthorized"),
	FORBIDDEN(403, "C403", "Forbidden"),
	NOT_FOUND(404, "C404", "Not Found"),
	CONFLICT(409, "B409", "Conflict"),

	// Business Errors,
	INVALID_INPUT(400, "B400", "Invalid Input Data"),
	DUPLICATE_ENTRY(400, "B401", "Duplicate Entry"),
	INSUFFICIENT_BALANCE(400, "B402", "Insufficient Balance"),
	DUPLICATE_REQUEST(429, "B429", "Duplicate Request"),

	// Server Errors
	INTERNAL_ERROR(500, "E500", "Internal Server Error"),
	SERVICE_UNAVAILABLE(503, "E503", "Service Unavailable");

	private final int status;
	private final String code;
	private final String message;
}
