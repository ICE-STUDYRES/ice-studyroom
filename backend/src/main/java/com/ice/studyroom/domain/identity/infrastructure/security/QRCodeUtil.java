package com.ice.studyroom.domain.identity.infrastructure.security;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Component;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.ice.studyroom.domain.identity.domain.service.EncryptionService;

@Component
public class QRCodeUtil {
	private static final int WIDTH = 250;
	private static final int HEIGHT = 250;
	private final EncryptionService encryptionService;

	public QRCodeUtil(EncryptionService encryptionService) {
		this.encryptionService = encryptionService;
	}

	// AES-256 암호화 후 QR 코드 생성
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

	// QR 코드 복호화 후 원본 데이터 반환
	public String decryptQRCode(String base64QrCode) {
		try {
			// Base64 디코딩 → BufferedImage 변환
			byte[] qrCodeBytes = Base64.getDecoder().decode(base64QrCode);
			ByteArrayInputStream inputStream = new ByteArrayInputStream(qrCodeBytes);
			BufferedImage qrImage = ImageIO.read(inputStream);

			// QR 코드 이미지에서 텍스트 추출
			LuminanceSource source = new BufferedImageLuminanceSource(qrImage);
			BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
			Result result = new MultiFormatReader().decode(bitmap);
			String encryptedData = result.getText();

			// AES-256 복호화
			return encryptionService.decrypt(encryptedData);
		} catch (Exception e) {
			throw new RuntimeException("QR 코드 복호화 오류", e);
		}
	}
}
