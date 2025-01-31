package com.ice.studyroom.domain.admin.presentation.dto.response;

import java.time.LocalDateTime;

import com.ice.studyroom.domain.membership.domain.type.PenaltyReasonType;

public record AdminPenaltyRecordResponse(
	PenaltyReasonType reason,
	LocalDateTime penaltyEnd
) {
	public static AdminPenaltyRecordResponse of(PenaltyReasonType reason, LocalDateTime penaltyEnd) {
		return new AdminPenaltyRecordResponse(reason, penaltyEnd);
	}
}
