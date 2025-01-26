package com.ice.studyroom.domain.admin.presentation.dto.response;

public record AdminDeleteOccupyResponse (
	String message
) {
	//factory 메서드를 통한 생성
	public static AdminDeleteOccupyResponse of(String message) {return new AdminDeleteOccupyResponse(message);}
}

