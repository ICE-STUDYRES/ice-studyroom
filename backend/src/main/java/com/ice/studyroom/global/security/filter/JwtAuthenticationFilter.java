package com.ice.studyroom.global.security.filter;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import com.ice.studyroom.global.security.jwt.JwtTokenProvider;
import com.ice.studyroom.global.exception.jwt.JwtAuthenticationException;
import com.ice.studyroom.global.security.handler.JwtAuthenticationEntryPoint;
import com.ice.studyroom.global.type.StatusCode;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends GenericFilterBean {
	private final JwtTokenProvider jwtTokenProvider;
	private final JwtAuthenticationEntryPoint authenticationEntryPoint;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws
		IOException,
		ServletException {

		HttpServletRequest httpRequest = (HttpServletRequest)request;
		HttpServletResponse httpResponse = (HttpServletResponse)response;

		String requestURI = httpRequest.getRequestURI();
		boolean isRefreshTokenRequest = "/api/users/auth/refresh".equals(requestURI);

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
					log.info("필터 내부 - 만료된 AT로 재발급 API 요청, URI: {}, Message: {}", httpRequest.getRequestURI(), e.getMessage());
					// Refresh API는 만료된 AT를 허용하고 내부 로직에서 재발급
					chain.doFilter(request, response);
					return;
				}

				// 일반 요청인데 만료된 AT라면 401 Unauthorized 응답, 프론트는 위의 조건문으로 전달될 수 있는 요청할 예정
				log.warn("필터 내부 - 만료된 AT로 일반 API 요청, URI: {}, Message: {}", httpRequest.getRequestURI(), e.getMessage());
				authenticationEntryPoint.commence(httpRequest, httpResponse,
					new JwtAuthenticationException(StatusCode.UNAUTHORIZED, "Access token has expired"));
				return;

			} catch (JwtAuthenticationException e) {
				log.warn("필터 내부 - JWT 인증 실패, URI: {}, Message: {}", httpRequest.getRequestURI(), e.getMessage());
				// ExceptionTranslationFilter의 도움으로 예외 핸들러로 전달 가능
				authenticationEntryPoint.commence(httpRequest, httpResponse, e);
				return;
			}
		}
		chain.doFilter(request, response);
	}

	// Request 헤더에서 토큰 정보 추출
	private String resolveToken(HttpServletRequest request) {
		String bearerToken = request.getHeader("Authorization");
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer")) {
			return bearerToken.substring(7);
		}
		return null;
	}
}
