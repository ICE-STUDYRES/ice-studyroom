package com.ice.studyroom.domain.penalty.interceptor;

import java.io.IOException;

import org.springframework.web.servlet.HandlerInterceptor;

import com.ice.studyroom.global.security.service.TokenService;
import com.ice.studyroom.domain.membership.domain.service.MemberDomainService;
import com.ice.studyroom.global.exception.BusinessException;
import com.ice.studyroom.global.type.StatusCode;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class PenaltyCheckInterceptor implements HandlerInterceptor {
	private final TokenService tokenService;
	private final MemberDomainService memberDomainService;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws
		IOException {
		String authorizationHeader = request.getHeader("Authorization");
		String email = tokenService.extractEmailFromAccessToken(authorizationHeader);

		if(memberDomainService.isMemberPenalty(email)){
			log.warn("요청 차단 - 제재 상태 사용자 접근 시도 - userEmail: {}, url: {}", email, request.getRequestURI());
			throw new BusinessException(StatusCode.PENALIZED_USER, "제재를 받은 사용자입니다.");
		}
		return true;
	}
}
