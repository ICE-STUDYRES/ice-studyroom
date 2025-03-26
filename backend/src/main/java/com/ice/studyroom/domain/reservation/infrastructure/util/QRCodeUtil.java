package com.ice.studyroom.domain.reservation.infrastructure.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Component;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;

@Component
public class QRCodeUtil {

	private static final int WIDTH = 250;
	private static final int HEIGHT = 250;

	// QR 토큰 문자열을 이미지(Base64)로 생성
	public String generateQRCodeFromToken(String token) {
		try {
			BitMatrix bitMatrix = new MultiFormatWriter()
				.encode(token, BarcodeFormat.QR_CODE, WIDTH, HEIGHT);
			BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			ImageIO.write(qrImage, "png", outputStream);
			return Base64.getEncoder().encodeToString(outputStream.toByteArray());

		} catch (Exception e) {
			throw new RuntimeException("QR 코드 생성 실패", e);
		}
	}

	// QR Base64 이미지를 디코딩하여 토큰 문자열 추출
	public String decodeQRCodeImage(String base64QrCode) {
		try {
			byte[] qrCodeBytes = Base64.getDecoder().decode(base64QrCode);
			ByteArrayInputStream inputStream = new ByteArrayInputStream(qrCodeBytes);
			BufferedImage qrImage = ImageIO.read(inputStream);

			if (qrImage == null) {
				throw new RuntimeException("QR 이미지 디코딩 실패 (null 이미지)");
			}

			BinaryBitmap bitmap = new BinaryBitmap(
				new HybridBinarizer(new BufferedImageLuminanceSource(qrImage)));
			Result result = new MultiFormatReader().decode(bitmap);
			return result.getText(); // = 토큰

		} catch (Exception e) {
			throw new RuntimeException("QR 코드 디코딩 실패", e);
		}
	}
}
