package com.ice.studyroom.global.type;

import lombok.Getter;

@Getter
public enum ActionType {
	VACANCY_ALERT("빈자리 알림 등록"),
	INDIVIDUAL_RESERVATION("개인 예약"),
	GROUP_RESERVATION("그룹 예약"),
	PASSWORD_RESET("비밀번호 재설정");

	private final String description;

	ActionType(String description) {
		this.description = description;
	}
}
