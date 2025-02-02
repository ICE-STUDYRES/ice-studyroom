package com.ice.studyroom.domain.identity.infrastructure.security;

import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AESUtil {

	private final SecretKey secretKey;
	private final byte[] salt;

	public AESUtil(@Value("${AESUTIL_SECRET_KEY}") String secretKey,
		@Value("${AESUTIL_SALT}") String salt
	) {
		this.secretKey = generateSecretKey(secretKey);
		this.salt = Base64.getDecoder().decode(salt);
	}

	public SecretKey generateSecretKey(String key) {
		try {
			byte[] decodedKey = Base64.getDecoder().decode(key);
			return new SecretKeySpec(decodedKey, "AES");
		} catch (Exception e) {
			throw new RuntimeException("키 생성 오류", e);
		}
	}

	public SecretKey getSecretKey() {
		return this.secretKey;
	}

	public byte[] getSalt() {
		return this.salt;
	}
}
