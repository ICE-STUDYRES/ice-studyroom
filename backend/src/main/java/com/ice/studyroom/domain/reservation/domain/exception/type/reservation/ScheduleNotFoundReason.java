package com.ice.studyroom.domain.reservation.domain.exception.type.reservation;

public enum ScheduleNotFoundReason {
	NOT_FOUND("존재하지 않는 스케줄입니다.");

	private final String message;

	ScheduleNotFoundReason(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
}
