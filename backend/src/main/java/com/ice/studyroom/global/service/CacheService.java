package com.ice.studyroom.global.service;

import java.time.Duration;

public interface CacheService {
	void save(String key, String value, Duration duration);

	String get(String key);

	void delete(String key);

	boolean exists(String key);
}
