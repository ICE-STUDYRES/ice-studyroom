package com.ice.studyroom.domain.penalty.domain.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PenaltyReasonType {
	CANCEL(2),
	LATE(3),
	NO_SHOW(7),
	ADMIN(0); //admin이 부여하는 경우는 해당 값으로 정하지 않는다.

	private final int durationDays;
}
