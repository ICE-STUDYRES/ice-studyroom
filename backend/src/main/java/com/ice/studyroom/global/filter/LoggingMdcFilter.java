package com.ice.studyroom.global.filter;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.ice.studyroom.global.security.service.TokenService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LoggingMdcFilter extends OncePerRequestFilter {

	private final TokenService tokenService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {
		try {
			String requestId = UUID.randomUUID().toString();
			MDC.put("requestId", requestId);

			String token = request.getHeader("Authorization");
			if (token != null && token.startsWith("Bearer ")) {
				String email = tokenService.extractEmailFromAccessToken(token);
				MDC.put("userEmail", email);
			}

			filterChain.doFilter(request, response);
		} finally {
			MDC.clear();
		}
	}
}
