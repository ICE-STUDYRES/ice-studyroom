package com.ice.studyroom.domain.membership.domain.exception.member;

import com.ice.studyroom.global.exception.BusinessException;
import com.ice.studyroom.global.type.StatusCode;
import lombok.Getter;

@Getter
public class MemberPenaltyException extends BusinessException {
	private final String email;

	public MemberPenaltyException(String message, String email) {
		super(StatusCode.FORBIDDEN, message);
		this.email = email;
	}
}
