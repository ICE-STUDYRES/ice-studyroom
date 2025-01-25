package com.ice.studyroom.domain.identity.infrastructure.security;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Component;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.ice.studyroom.domain.identity.domain.service.EncryptionService;

@Component
public class QRCodeUtil {
	private static final int WIDTH = 250;
	private static final int HEIGHT = 250;
	private final EncryptionService encryptionService;

	public QRCodeUtil(EncryptionService encryptionService) {
		this.encryptionService = encryptionService;
	}

	// 🔹 AES-256 암호화 후 QR 코드 생성
	public String generateQRCode(String email, String reservationId) {
		try {
			String originalData = email + "_" + reservationId;
			String encryptedData = encryptionService.encrypt(originalData); // AES-256 암호화

			BitMatrix bitMatrix = new MultiFormatWriter().encode(encryptedData, BarcodeFormat.QR_CODE, WIDTH, HEIGHT);
			BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			ImageIO.write(qrImage, "png", outputStream);
			return Base64.getEncoder().encodeToString(outputStream.toByteArray());

		} catch (WriterException | java.io.IOException e) {
			throw new RuntimeException("QR 코드 생성 실패", e);
		}
	}

	// 🔹 QR 코드 복호화 후 원본 데이터 반환
	public String decryptQRCode(String encryptedData) {
		return encryptionService.decrypt(encryptedData);
	}
}
