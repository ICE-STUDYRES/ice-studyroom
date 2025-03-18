package com.ice.studyroom.domain.admin.presentation.dto.response;

import java.time.LocalDateTime;

import com.ice.studyroom.domain.penalty.domain.type.PenaltyReasonType;

public record AdminPenaltyRecordResponse(
	String userName,
	String email,
	String studentNum,
	PenaltyReasonType reason,
	LocalDateTime penaltyEnd
) {
	public static AdminPenaltyRecordResponse of(String userName, String email, String studentNum,
		PenaltyReasonType reason, LocalDateTime penaltyEnd) {
		return new AdminPenaltyRecordResponse(userName, email, studentNum, reason, penaltyEnd);
	}
}
