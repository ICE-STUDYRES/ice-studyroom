package com.ice.studyroom.domain.schedule.domain.exception.schedule;

import com.ice.studyroom.global.exception.BusinessException;
import com.ice.studyroom.global.type.StatusCode;

public class GroupRoomOnlyException extends BusinessException {
	public GroupRoomOnlyException(String message) {
		super(StatusCode.FORBIDDEN, message);
	}
}
