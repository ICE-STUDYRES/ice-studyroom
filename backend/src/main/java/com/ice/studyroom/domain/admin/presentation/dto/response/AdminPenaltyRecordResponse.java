package com.ice.studyroom.domain.admin.presentation.dto.response;

import java.time.LocalDateTime;

public record AdminPenaltyRecordResponse(
	String reason,
	LocalDateTime penaltyEnd
) {
	public static AdminPenaltyRecordResponse of(String reason, LocalDateTime penaltyEnd) {
		return new AdminPenaltyRecordResponse(reason, penaltyEnd);
	}
}
