package com.ice.studyroom.domain.identity.domain.service;

import java.time.Duration;

import org.springframework.stereotype.Service;

import com.ice.studyroom.global.service.CacheService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VerificationCodeCacheService {
	private final CacheService cacheService;

	private static final Duration DEFAULT_TTL = Duration.ofMinutes(5);

	public void saveVerificationCode(String email, String code) {
		cacheService.save(email, code, DEFAULT_TTL);
	}

	public void saveVerificationCode(String email, String code, Duration ttl) {
		cacheService.save(email, code, ttl);
	}

	public String getVerificationCode(String email) {
		return cacheService.get(email);
	}

	public boolean existsVerificationCode(String email) {
		return cacheService.exists(email);
	}
}
