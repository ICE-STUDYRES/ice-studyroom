package com.ice.studyroom.domain.notification.application;

import com.ice.studyroom.domain.membership.domain.vo.Email;
import com.ice.studyroom.domain.membership.infrastructure.persistence.MemberRepository;
import com.ice.studyroom.domain.notification.infrastructure.NotificationRepository;
import com.ice.studyroom.domain.notification.presentation.dto.response.NotificationResponse;
import com.ice.studyroom.global.security.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationQueryService {

	private final NotificationRepository notificationRepository;
	private final TokenService tokenService;
	private final MemberRepository memberRepository;

	public List<NotificationResponse> getUnreadNotifications(String authorizationHeader) {

		String email = tokenService.extractEmailFromAccessToken(authorizationHeader);

		Long memberId = memberRepository.findByEmail(Email.of(email))
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자"))
			.getId();

		return notificationRepository
			.findUnreadByMemberId(memberId)
			.stream()
			.map(NotificationResponse::from)
			.toList();
	}
}
