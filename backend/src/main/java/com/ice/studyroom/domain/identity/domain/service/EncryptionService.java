package com.ice.studyroom.domain.identity.domain.service;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ice.studyroom.domain.identity.infrastructure.security.AESUtil;

@Service
public class EncryptionService {
	private final AESUtil aesUtil;
	private static final int SALT_LENGTH = 16;
	private static final int IV_LENGTH = 16;

	@Autowired
	public EncryptionService(AESUtil aesUtil) {
		this.aesUtil = aesUtil;
	}

	public String encrypt(String strToEncrypt) {
		try {
			byte[] salt = generateRandomBytes(SALT_LENGTH);
			byte[] iv = generateRandomBytes(IV_LENGTH);
			IvParameterSpec ivspec = new IvParameterSpec(iv);

			SecretKey secretKey = aesUtil.generateSecretKey(salt);

			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);
			byte[] encrypted = cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8));

			return encodeBase64(salt, iv, encrypted);
		} catch (Exception e) {
			throw new RuntimeException("암호화 오류", e);
		}
	}

	public String decrypt(String strToDecrypt) {
		try {
			byte[] decoded = Base64.getDecoder().decode(strToDecrypt);

			byte[] salt = extractBytes(decoded, 0, SALT_LENGTH);
			byte[] iv = extractBytes(decoded, SALT_LENGTH, IV_LENGTH);
			IvParameterSpec ivspec = new IvParameterSpec(iv);
			byte[] encrypted = extractBytes(decoded, SALT_LENGTH + IV_LENGTH, decoded.length - SALT_LENGTH - IV_LENGTH);

			SecretKey secretKey = aesUtil.generateSecretKey(salt);

			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec);
			byte[] decrypted = cipher.doFinal(encrypted);

			return new String(decrypted, StandardCharsets.UTF_8);
		} catch (Exception e) {
			throw new RuntimeException("복호화 오류", e);
		}
	}

	// SALT, IV 등을 안전하게 생성하는 메서드
	private byte[] generateRandomBytes(int length) {
		byte[] bytes = new byte[length];
		new SecureRandom().nextBytes(bytes);
		return bytes;
	}

	// Base64로 인코딩하여 SALT + IV + 암호문을 안전하게 저장
	private String encodeBase64(byte[] salt, byte[] iv, byte[] encrypted) {
		byte[] combined = new byte[salt.length + iv.length + encrypted.length];
		System.arraycopy(salt, 0, combined, 0, salt.length);
		System.arraycopy(iv, 0, combined, salt.length, iv.length);
		System.arraycopy(encrypted, 0, combined, salt.length + iv.length, encrypted.length);
		return Base64.getEncoder().encodeToString(combined);
	}

	// Base64로 저장된 데이터에서 특정 바이트 배열을 추출하는 메서드
	private byte[] extractBytes(byte[] source, int start, int length) {
		byte[] result = new byte[length];
		System.arraycopy(source, start, result, 0, length);
		return result;
	}
}
