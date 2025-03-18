package com.ice.studyroom.domain.penalty.domain.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PenaltyReasonType {
	CANCEL(2),
	LATE(3),
	NO_SHOW(5);

	private final int durationDays;
}
