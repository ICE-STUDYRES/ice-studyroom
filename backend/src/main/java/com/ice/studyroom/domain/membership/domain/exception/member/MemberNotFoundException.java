package com.ice.studyroom.domain.membership.domain.exception.member;

import com.ice.studyroom.global.exception.BusinessException;
import com.ice.studyroom.global.type.ActionType;
import com.ice.studyroom.global.type.StatusCode;
import lombok.Getter;

@Getter
public class MemberNotFoundException extends BusinessException {
	private final String requesterEmail;
	private final String description;

	public MemberNotFoundException(String requesterEmail, ActionType actionType) {
		super(StatusCode.NOT_FOUND, "사용자를 찾을 수 없습니다.");
		this.requesterEmail = requesterEmail;
		this.description = actionType.getDescription();
	}
}
