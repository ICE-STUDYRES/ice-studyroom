package com.ice.studyroom.domain.identity.domain.service;

import org.springframework.stereotype.Service;

import com.ice.studyroom.global.service.CacheService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VerificationCodeCacheService {
	private final CacheService cacheService;

	public String getVerificationCode(String email) {
		return cacheService.get(email);
	}
}
