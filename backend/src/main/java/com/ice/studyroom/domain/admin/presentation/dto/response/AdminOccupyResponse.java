package com.ice.studyroom.domain.admin.presentation.dto.response;

public record AdminOccupyResponse(String message) {

	public static AdminOccupyResponse of(String message) {
		return new AdminOccupyResponse(message);
	}
}
