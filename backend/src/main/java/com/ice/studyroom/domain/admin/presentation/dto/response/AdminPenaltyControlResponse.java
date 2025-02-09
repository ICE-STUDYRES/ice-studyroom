package com.ice.studyroom.domain.admin.presentation.dto.response;

public record AdminPenaltyControlResponse(
String message
) {
	public static AdminPenaltyControlResponse of(String message) {return new AdminPenaltyControlResponse(message);}
}
