package com.ice.studyroom.domain.reservation.application.exception;

import com.ice.studyroom.global.exception.BusinessException;
import com.ice.studyroom.global.type.StatusCode;
import lombok.Getter;

@Getter
public class ParticipantAlreadyReservedException extends BusinessException {
	private final String email;

	public ParticipantAlreadyReservedException(String message, String email) {
		super(StatusCode.BAD_REQUEST, message);
		this.email = email;
	}
}
