package com.ice.studyroom.domain.notification.domain.exception;

import com.ice.studyroom.global.exception.BusinessException;
import com.ice.studyroom.global.type.StatusCode;

public class NotificationNotFoundException extends BusinessException {

	private final Long notificationId;

	public NotificationNotFoundException(Long notificationId) {
		super(StatusCode.NOT_FOUND,
			"해당 알림을 찾을 수 없습니다.");
		this.notificationId = notificationId;
	}
}
