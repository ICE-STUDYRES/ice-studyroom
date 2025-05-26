package com.ice.studyroom.global.exception;

import com.ice.studyroom.global.type.StatusCode;

public class RoomNotFoundException extends BusinessException {
	public RoomNotFoundException(String roomNumber) {
		super(StatusCode.NOT_FOUND, "roomNumber '" + roomNumber + "'에 해당하는 방이 존재하지 않습니다.");
	}
}
