package com.ice.studyroom.domain.admin.presentation.dto.response;

import com.ice.studyroom.domain.admin.presentation.dto.request.AdminCreateReserveRequest;

public record AdminCreateReserveResponse (
	String message
	) {
	//factory 메서드를 통한 생성
	public static AdminCreateReserveResponse of(String message) {return new AdminCreateReserveResponse(message);}
}
