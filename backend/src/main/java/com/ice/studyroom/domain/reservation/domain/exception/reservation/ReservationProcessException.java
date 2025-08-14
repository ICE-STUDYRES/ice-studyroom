package com.ice.studyroom.domain.reservation.domain.exception.reservation;

import com.ice.studyroom.global.exception.BusinessException;
import com.ice.studyroom.global.type.StatusCode;
import lombok.Getter;

import java.util.List;

@Getter
public class ReservationProcessException extends BusinessException {
	private final String reservationOwnerEmail;
	private final List<Long> scheduleIds;

	public ReservationProcessException(String reservationOwnerEmail, List<Long> scheduleIds, String originalCause) {
		super(StatusCode.INTERNAL_ERROR, buildMessage(originalCause));
		this.reservationOwnerEmail = reservationOwnerEmail;
		this.scheduleIds = scheduleIds;
	}

	private static String buildMessage(String originalCause) {
		return "예약 처리 중 오류가 발생하여 모든 변경사항이 롤백됩니다. 사유: " + originalCause;
	}
}
