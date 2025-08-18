package com.ice.studyroom.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class RedisConfig {

	@Value("${spring.redis.host}")
	private String host;

	@Value("${spring.redis.port}")
	private int port;

	@Value("${spring.redis.password}")
	private String password;

	@Value("${spring.redis.timeout:1000}")
	private int timeout;

	@Bean
	public RedisConnectionFactory redisConnectionFactory() {
		RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration(host, port);
		redisConfig.setPassword(password);

		LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
			.commandTimeout(Duration.ofMillis(timeout))
			.shutdownTimeout(Duration.ofMillis(100))
			.build();

		return new LettuceConnectionFactory(redisConfig, clientConfig);
	}

	@Bean
	public RedisTemplate<String, Object> redisTemplate() {
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(redisConnectionFactory());

		// 일반적인 key:value의 경우 시리얼라이저
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new StringRedisSerializer());

		// Hash를 사용할 경우 시리얼라이저
		redisTemplate.setHashKeySerializer(new StringRedisSerializer());
		redisTemplate.setHashValueSerializer(new StringRedisSerializer());

		// 모든 경우
		redisTemplate.setDefaultSerializer(new StringRedisSerializer());

		return redisTemplate;
	}
}
