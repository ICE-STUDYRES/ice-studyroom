package com.ice.studyroom.domain.schedule.domain.exception.schedule;

import com.ice.studyroom.global.exception.BusinessException;
import com.ice.studyroom.global.type.ActionType;
import com.ice.studyroom.global.type.StatusCode;
import lombok.Getter;

import java.util.List;

@Getter
public class ScheduleNotFoundException extends BusinessException {
	private final Long scheduleId;
	private final List<Long> scheduleIds;
	private final String requesterEmail;
	private final String description;

	public ScheduleNotFoundException(Long scheduleId, String requesterEmail, ActionType actionType) {
		super(StatusCode.NOT_FOUND, "유효하지않은 스케줄입니다.");
		this.scheduleId = scheduleId;
		this.scheduleIds = null;
		this.requesterEmail = requesterEmail;
		this.description = actionType.getDescription();
	}

	// 복수 스케줄용 생성자
	public ScheduleNotFoundException(List<Long> scheduleIds, ActionType actionType) {
		super(StatusCode.NOT_FOUND, "존재하지 않는 스케줄이 포함되어 있습니다.");
		this.scheduleId = null;
		this.scheduleIds = scheduleIds;
		this.requesterEmail = null;
		this.description = actionType.getDescription();
	}
}
