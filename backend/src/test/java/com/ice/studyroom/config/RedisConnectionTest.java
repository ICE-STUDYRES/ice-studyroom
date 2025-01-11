package com.ice.studyroom.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
class RedisConnectionTest {
	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@Test
	void connectionTest() {
		String key = "test";
		String value = "hello";
		redisTemplate.opsForValue().set(key, value);

		String result = (String)redisTemplate.opsForValue().get(key);
		assertEquals(value, result);
	}
}
