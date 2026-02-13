package com.ice.studyroom.domain.notification.application;

import com.ice.studyroom.domain.membership.domain.entity.Member;
import com.ice.studyroom.domain.membership.domain.vo.Email;
import com.ice.studyroom.domain.membership.infrastructure.persistence.MemberRepository;
import com.ice.studyroom.domain.notification.domain.exception.NotificationNotFoundException;
import com.ice.studyroom.domain.notification.domain.entity.Notification;
import com.ice.studyroom.domain.notification.infrastructure.NotificationRepository;
import com.ice.studyroom.domain.notification.presentation.dto.response.NotificationResponse;
import com.ice.studyroom.global.exception.BusinessException;
import com.ice.studyroom.global.security.service.TokenService;
import com.ice.studyroom.global.type.StatusCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationQueryService {

	private final NotificationRepository notificationRepository;
	private final MemberRepository memberRepository;
	private final TokenService tokenService;

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

	@Transactional
	public String readNotification(Long notificationId, String authorizationHeader) {

		String email = tokenService.extractEmailFromAccessToken(authorizationHeader);

		Member member = memberRepository.findByEmail(Email.of(email))
			.orElseThrow(() ->
				new BusinessException(StatusCode.NOT_FOUND, "존재하지 않는 사용자입니다.")
			);

		Notification notification = notificationRepository
			.findForRead(notificationId, member.getId())
			.orElseThrow(() ->
				new NotificationNotFoundException(notificationId)
			);

		notification.markAsRead();

		return "알림 읽음 처리 성공";
	}

	@Transactional
	public String readAllNotifications(String authorizationHeader) {

		String email = tokenService.extractEmailFromAccessToken(authorizationHeader);

		Member member = memberRepository.findByEmail(Email.of(email))
			.orElseThrow(() ->
				new BusinessException(StatusCode.NOT_FOUND, "존재하지 않는 사용자입니다.")
			);

		int updatedCount = notificationRepository.markAllAsRead(member.getId());

		return updatedCount + "개의 알림이 읽음 처리되었습니다.";
	}

}
