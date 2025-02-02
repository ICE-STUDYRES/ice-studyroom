package com.ice.studyroom.domain.identity.domain.service;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ice.studyroom.domain.identity.infrastructure.security.AESUtil;

@Service
public class EncryptionService {
	private final SecretKey secretKey;
	private final byte[] salt;
	private static final int IV_LENGTH = 16;

	@Autowired
	public EncryptionService(AESUtil aesUtil) {
		this.secretKey = aesUtil.getSecretKey();
		this.salt = aesUtil.getSalt();
	}

	public String encrypt(String strToEncrypt) {
		try {
			byte[] iv = generateRandomBytes(IV_LENGTH); // 랜덤 IV 생성
			IvParameterSpec ivspec = new IvParameterSpec(iv);

			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);
			byte[] encrypted = cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8));

			return encodeBase64(salt, iv, encrypted); // 고정된 salt + IV + 암호문을 Base64 인코딩
		} catch (Exception e) {
			throw new RuntimeException("암호화 오류", e);
		}
	}

	public String decrypt(String strToDecrypt) {
		try {
			byte[] decoded = Base64.getDecoder().decode(strToDecrypt);
			byte[] extractedSalt = extractBytes(decoded, 0, salt.length);
			byte[] iv = extractBytes(decoded, salt.length, IV_LENGTH);
			IvParameterSpec ivspec = new IvParameterSpec(iv);
			byte[] encrypted = extractBytes(decoded, salt.length + IV_LENGTH, decoded.length - salt.length - IV_LENGTH);
			// salt 검증 (올바른 salt인지 확인)
			if (!Arrays.equals(salt, extractedSalt)) {
				throw new RuntimeException("복호화 오류: 잘못된 salt 값");
			}

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
