package com.ice.studyroom.domain.identity.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	private final JwtTokenProvider jwtTokenProvider;

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
		return httpSecurity
			.httpBasic(AbstractHttpConfigurer::disable)
			.csrf(AbstractHttpConfigurer::disable)
			.sessionManagement(sessionManagement ->
				sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(authorize -> authorize
				// 인증 없이 접근 가능한 엔드포인트
				.requestMatchers(
					HttpMethod.POST, "/api/users", // 회원가입
					"/api/users/login", // 로그인
					"/api/users/email-verification", // 이메일 인증 메일 전송
					"/api/users/email-verification/confirm" // 이메일 인증 코드 검증
				).permitAll()

				// 인증이 필요한 일반 API
				.requestMatchers(
					"/api/users/**",
					"/api/qr/**",
					"/api/schedules/**",
					"/api/reservations/**"
				).authenticated()

				// ADMIN 역할만 접근 가능
				.requestMatchers("/api/admin/**").hasRole("ADMIN")

				// ATTENDANT 역할만 접근 가능
				.requestMatchers("/api/qr/recognize").hasRole("ATTENDANT")

				// Swagger 관련 경로 허용
				.requestMatchers(
					"/swagger-ui.html",
					"/swagger-ui/**",
					"/v3/api-docs/**"
				).permitAll()
			)
			.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
				UsernamePasswordAuthenticationFilter.class)
			.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}
}

