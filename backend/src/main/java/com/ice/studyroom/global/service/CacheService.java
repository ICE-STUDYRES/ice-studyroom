package com.ice.studyroom.global.service;

import java.time.Duration;

public interface CacheService {
	public void save(String key, String value, Duration duration);

	public String get(String key);

	public void delete(String key);

	public boolean exists(String key);
}
