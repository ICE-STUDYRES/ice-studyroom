package com.ice.studyroom.domain.reservation.domain.exception.reservation;

import com.ice.studyroom.domain.reservation.domain.exception.type.reservation.ReservationActionType;
import com.ice.studyroom.domain.reservation.domain.exception.type.reservation.ScheduleNotFoundReason;
import com.ice.studyroom.global.exception.BusinessException;
import com.ice.studyroom.global.type.StatusCode;
import lombok.Getter;

/**
 * 요청한 Member 의 소유가 아닌 Reservation인 경우에는 접근할 수 없기에 예외 발생
 */
@Getter
public final class ReservationScheduleNotFoundException extends BusinessException {
	/**
	 * 어떤 기능을 서빙하다가 발생한 예외인지
	 */
	private final String description;
	/**
	 * 요청한 스케줄 ID
	 */
	private final Long scheduleId;
	/**
	 * 연관된 예약 ID
	 */
	private final Long reservationId;

	public ReservationScheduleNotFoundException(ScheduleNotFoundReason reason, Long scheduleId, Long reservationId, ReservationActionType actionType) {
		super(StatusCode.NOT_FOUND, reason.getMessage());
		this.description = actionType.getDescription();
		this.scheduleId = scheduleId;
		this.reservationId = reservationId;
	}
}
