package com.ice.studyroom.global.security.handler;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {
	private final Clock clock;
	@Override
	public void handle(HttpServletRequest request,
		HttpServletResponse response,
		AccessDeniedException accessDeniedException) throws IOException, ServletException {

		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		String json = """
			{
				"code": "FORBIDDEN",
				"message": "You do not have permission to access this resource.",
				"data": null,
				"errors": null,
				"timestamp": "%s"
			}
			""".formatted(LocalDateTime.now(clock));

		response.getWriter().write(json);
	}
}
