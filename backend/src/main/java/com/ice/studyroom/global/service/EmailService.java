package com.ice.studyroom.global.service;

import org.springframework.scheduling.annotation.Async;

import com.ice.studyroom.global.dto.request.EmailRequest;

public interface EmailService {

	@Async("emailTaskExecutor")
	void sendEmail(EmailRequest emailRequest);
}
