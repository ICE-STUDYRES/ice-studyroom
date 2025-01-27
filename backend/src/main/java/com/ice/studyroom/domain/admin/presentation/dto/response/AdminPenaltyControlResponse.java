package com.ice.studyroom.domain.admin.presentation.dto.response;

public record AdminPenaltyControlResponse(
String message,
int penaltyCount
) {
	public static AdminPenaltyControlResponse of(String message, int penaltyCount) {return new AdminPenaltyControlResponse(message, penaltyCount);}
}
