package com.ice.studyroom.config;

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

import com.ice.studyroom.global.security.filter.JwtAuthenticationFilter;
import com.ice.studyroom.global.security.jwt.JwtTokenProvider;
import com.ice.studyroom.global.security.handler.JwtAccessDeniedHandler;
import com.ice.studyroom.global.security.handler.JwtAuthenticationEntryPoint;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	private final JwtTokenProvider jwtTokenProvider;
	private final JwtAuthenticationEntryPoint authenticationEntryPoint;
	private final JwtAccessDeniedHandler accessDeniedHandler;

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
			.exceptionHandling(exceptionHandling -> exceptionHandling
				.authenticationEntryPoint(authenticationEntryPoint) // 401 에러 핸들러
				.accessDeniedHandler(accessDeniedHandler)           // 403 에러 핸들러
			)
			.authorizeHttpRequests(authorize -> authorize

				// ADMIN 역할만 접근 가능
				.requestMatchers("/api/admin/**").hasRole("ADMIN")

				// ATTENDANT 역할만 접근 가능
				.requestMatchers("/api/qr/recognize").hasRole("ATTENDANT")

				// 인증 없이 접근 가능한 엔드포인트
				.requestMatchers(HttpMethod.POST, "/api/users").permitAll() // 회원가입
				.requestMatchers(HttpMethod.POST, "/api/users/login").permitAll() // 로그인
				.requestMatchers(HttpMethod.POST, "/api/users/email-verification").permitAll() // 이메일 인증 메일 전송
				.requestMatchers(HttpMethod.POST, "/api/users/email-verification/confirm").permitAll() // 이메일 인증 코드 검증

				//Refresh Token 검증을 위한 별도 처리
				.requestMatchers(HttpMethod.POST, "/api/users/auth/refresh").permitAll() // Refresh Token 발급

				// 그 외, 인증이 필요한 일반 API
				.requestMatchers(
					"/api/users/**",
					"/api/schedules/**",
					"/api/reservations/**"
				).authenticated()

				// Swagger 관련 경로 허용
				.requestMatchers(
					"/swagger-ui.html",
					"/swagger-ui/**",
					"/v3/api-docs/**"
				).permitAll()

				// prometheus 관련 경로 허용
				.requestMatchers(
					"/actuator/prometheus",
					"/actuator/**"
				).permitAll()
			)
			.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
			.build();
	}

	@Bean
	public JwtAuthenticationFilter jwtAuthenticationFilter() {
		return new JwtAuthenticationFilter(jwtTokenProvider, authenticationEntryPoint);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}
}

