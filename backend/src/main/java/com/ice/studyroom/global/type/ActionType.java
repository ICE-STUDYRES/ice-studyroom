package com.ice.studyroom.global.type;

import lombok.Getter;

@Getter
public enum ActionType {
	VACANCY_ALERT("빈자리 알림 등록");

	private final String description;

	ActionType(String description) {
		this.description = description;
	}
}
