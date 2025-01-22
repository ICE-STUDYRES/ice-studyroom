package com.ice.studyroom.domain.admin.presentation.dto.response;

public record AdminCreateOccupyResponse (
	String message
) {
	//factory 메서드를 통한 생성
	public static AdminCreateOccupyResponse of(String message) {return new AdminCreateOccupyResponse(message);}
}
