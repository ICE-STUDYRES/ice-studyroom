package com.ice.studyroom.global.service;

import com.ice.studyroom.global.dto.request.EmailRequest;

public interface EmailService {
	void sendEmail(EmailRequest emailRequest);
}
