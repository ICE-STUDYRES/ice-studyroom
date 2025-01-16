package com.ice.studyroom.global.controller;

import com.ice.studyroom.global.service.EmailService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
public class EmailController {

	private final EmailService emailService;

	public EmailController(EmailService emailService) {
		this.emailService = emailService;
	}

	// 테스트용 메일 전송 API
	@PostMapping("/send")
	public ResponseEntity<String> sendEmail(
		@RequestParam String to,
		@RequestParam String subject,
		@RequestParam String body
	) {
		try {
			emailService.sendEmail(to, subject, body);
			return ResponseEntity.ok("이메일 전송 요청이 처리되었습니다.");
		} catch (Exception e) {
			// 예외 메시지를 로그로 출력
			System.err.println("메일 전송 중 오류 발생: " + e.getMessage());
			e.printStackTrace();  // 스택 트레이스 출력

			// 클라이언트에 에러 응답 전송
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("메일 전송 중 오류가 발생했습니다: " + e.getMessage());
		}
	}
}
