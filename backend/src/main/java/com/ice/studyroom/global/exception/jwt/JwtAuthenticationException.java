package com.ice.studyroom.global.exception.jwt;

import org.springframework.security.core.AuthenticationException;

import com.ice.studyroom.global.type.StatusCode;

import lombok.Getter;

@Getter
public class JwtAuthenticationException extends AuthenticationException {
	private final StatusCode statusCode;

	public JwtAuthenticationException(StatusCode statusCode, String message) {
		super(message);
		this.statusCode = statusCode;
	}
}
