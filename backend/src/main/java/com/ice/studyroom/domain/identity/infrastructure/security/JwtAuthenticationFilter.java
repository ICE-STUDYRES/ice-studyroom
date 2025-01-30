package com.ice.studyroom.domain.identity.infrastructure.security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import com.ice.studyroom.domain.identity.exception.InvalidJwtException;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends GenericFilterBean {
	private final JwtTokenProvider jwtTokenProvider;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws
		IOException,
		ServletException {

		HttpServletRequest httpRequest = (HttpServletRequest)request;
		HttpServletResponse httpResponse = (HttpServletResponse)response;

		String requestURI = httpRequest.getRequestURI();
		boolean isRefreshTokenRequest = "/api/users/refresh".equals(requestURI);

		// 1. Request Header에서 JWT 토큰 추출
		String token = resolveToken((HttpServletRequest)request);


		// 2. validateToken으로 토큰 유효성 검사
		if (token != null) {
			try {
				jwtTokenProvider.validateToken(token); // 유효성 검사에서 예외 발생 시 catch로 이동

				Authentication authentication = jwtTokenProvider.getAuthentication(token);
				SecurityContextHolder.getContext().setAuthentication(authentication);
			} catch (ExpiredJwtException e) {
				if (isRefreshTokenRequest) {
					// 리프레시 토큰 요청이면 Access Token 만료 예외를 허용
					chain.doFilter(request, response);
					return;
				}
				setUnauthorizedResponse(httpResponse, "E401", "JWT token expired");
				return;
			} catch (InvalidJwtException e) {
				setUnauthorizedResponse(httpResponse, "E401", "Invalid JWT token");
				return;
			} catch (RuntimeException e) {
				setUnauthorizedResponse(httpResponse, "E500", "Authentication error");
				return;
			}
		}

		chain.doFilter(request, response);
	}

	// Request Header에서 토큰 정보 추출
	private String resolveToken(HttpServletRequest request) {
		String bearerToken = request.getHeader("Authorization");
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer")) {
			return bearerToken.substring(7);
		}
		return null;
	}

	private void setUnauthorizedResponse(HttpServletResponse response, String code, String message) throws IOException {
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		String jsonResponse = String.format(
			"{" +
				"\"code\": \"%s\"," +
				"\"message\": \"%s\"," +
				"\"data\": null," +
				"\"errors\": null," +
				"\"timestamp\": \"%s\"" +
				"}",
			code,
			message,
			java.time.LocalDateTime.now()
		);

		response.getWriter().write(jsonResponse);
		response.getWriter().flush();
	}
}
