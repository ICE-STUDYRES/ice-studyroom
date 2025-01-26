package com.ice.studyroom.domain.identity.exception;

public class InvalidJwtException extends RuntimeException {
	public InvalidJwtException(String message) {
		super(message);
	}

	public InvalidJwtException(String message, Throwable cause) {
		super(message, cause);
	}
}
