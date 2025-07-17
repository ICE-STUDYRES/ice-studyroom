package com.ice.studyroom.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.ice.studyroom.global.security.service.TokenService;
import com.ice.studyroom.domain.membership.domain.service.MemberDomainService;
import com.ice.studyroom.domain.penalty.interceptor.PenaltyCheckInterceptor;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

	private final TokenService tokenService;
	private final MemberDomainService memberDomainService;

	@Bean
	public PenaltyCheckInterceptor penaltyCheckInterceptor() {
		return new PenaltyCheckInterceptor(tokenService, memberDomainService);
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(penaltyCheckInterceptor())
			.order(1)
			.addPathPatterns("/api/**")
			.excludePathPatterns("/api/reservations/my/latest", "/api/reservations/my", "/api/users/**", "/api/qr/recognize");
	}
}
