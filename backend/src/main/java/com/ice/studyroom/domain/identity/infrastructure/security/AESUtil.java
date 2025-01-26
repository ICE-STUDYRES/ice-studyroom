package com.ice.studyroom.domain.identity.infrastructure.security;

import java.security.spec.KeySpec;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AESUtil {

	private final String secretKey;

	public AESUtil(@Value("${AESUTIL_SECRET_KEY}") String secretKey) {
		this.secretKey = secretKey;
	}

	public SecretKey generateSecretKey(byte[] salt) {
		try {
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			KeySpec spec = new PBEKeySpec(secretKey.toCharArray(), salt, 65536, 256);
			return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
		} catch (Exception e) {
			throw new RuntimeException("키 생성 오류", e);
		}
	}
}
