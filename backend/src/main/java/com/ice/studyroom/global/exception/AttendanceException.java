package com.ice.studyroom.global.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class AttendanceException extends RuntimeException {
	private final HttpStatus status;

	public AttendanceException(String message, HttpStatus status) {
		super(message);
		this.status = status;
	}

	public HttpStatus getStatus() {
		return status;
	}
}
