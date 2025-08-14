package com.ice.studyroom.domain.reservation.domain.exception.type.reservation;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReservationAccessDeniedReason {
	NOT_OWNER("유효하지 않은 사용자는 해당 예약에 접근할 수 없습니다.");

	private final String message;
}
