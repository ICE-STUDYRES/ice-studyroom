package com.ice.studyroom.global.security.handler;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {
	private final Clock clock;
	@Override
	public void handle(HttpServletRequest request,
		HttpServletResponse response,
		AccessDeniedException accessDeniedException) throws IOException, ServletException {

		// 인증된 사용자 정보 가져오기
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String email = null;

		// 인증된 사용자가 있을 때
		if (authentication != null) {
			// JWT에서 인증된 이메일을 가져올 수 있다면
			Object principal = authentication.getPrincipal();
			if (principal instanceof UserDetails) {
				email = ((UserDetails) principal).getUsername();  // 일반적으로 username이 이메일로 설정됨
			}
		}

		// 로그에 이메일 추가
		log.warn("접근 거부: {} {} - 사유: {} - 이메일: {}",
			request.getMethod(),
			request.getRequestURI(),
			accessDeniedException.getMessage(),
			email);

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
