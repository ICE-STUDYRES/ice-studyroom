package com.ice.studyroom.global.security.handler;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
	private final Clock clock;
	@Override
	public void commence(HttpServletRequest request,
		HttpServletResponse response,
		AuthenticationException authException) throws IOException, ServletException {

		String message = authException.getMessage();

		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		String json = """
			{
				"code": "UNAUTHORIZED",
				"message": "%s",
				"data": null,
				"errors": null,
				"timestamp": "%s"
			}
			""".formatted(message, LocalDateTime.now(clock));

		response.getWriter().write(json);
	}
}
