package com.ice.studyroom.domain.reservation.domain.exception.reservation;

import com.ice.studyroom.domain.reservation.domain.exception.type.reservation.ReservationActionType;
import com.ice.studyroom.domain.reservation.domain.exception.type.reservation.ReservationNotFoundReason;
import com.ice.studyroom.global.exception.BusinessException;
import com.ice.studyroom.global.type.StatusCode;

/**
 * 요청한 Member 의 소유가 아닌 Reservation인 경우에는 접근할 수 없기에 예외 발생
 */
public class ReservationNotFoundException extends BusinessException {
	/**
	 * 어떤 기능을 서빙하다가 발생한 예외인지
	 */
	private final String description;
	/**
	 * 요청한 예약 ID
	 */
	private final Long reservationId;
	/**
	 * 예약에 대한 접근을 '시도한' 사용자의 이메일
	 */
	private final String requesterEmail;

	public ReservationNotFoundException(ReservationNotFoundReason reason, Long reservationId, String requesterEmail, ReservationActionType actionType) {
		super(StatusCode.NOT_FOUND, reason.getMessage());
		this.description = actionType.getDescription();
		this.reservationId = reservationId;
		this.requesterEmail = requesterEmail;
	}

	public Long getReservationId() {
		return reservationId;
	}
	public String getRequesterEmail() { return requesterEmail; }
	public String getDescription() { return description; }
}
