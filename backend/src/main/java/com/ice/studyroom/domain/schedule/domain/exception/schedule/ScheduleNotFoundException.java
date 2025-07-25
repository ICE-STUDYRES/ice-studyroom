package com.ice.studyroom.domain.schedule.domain.exception.schedule;

import com.ice.studyroom.global.exception.BusinessException;
import com.ice.studyroom.global.type.ActionType;
import com.ice.studyroom.global.type.StatusCode;
import lombok.Getter;

@Getter
public class ScheduleNotFoundException extends BusinessException {
	private final Long scheduleId;
	private final String requesterEmail;
	private final String description;

	public ScheduleNotFoundException(Long scheduleId, String requesterEmail, ActionType actionType) {
		super(StatusCode.NOT_FOUND, "유효하지않은 스케줄입니다.");
		this.scheduleId = scheduleId;
		this.requesterEmail = requesterEmail;
		this.description = actionType.getDescription();
	}
}
