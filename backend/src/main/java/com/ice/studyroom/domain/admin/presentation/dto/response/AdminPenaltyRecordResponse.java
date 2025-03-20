package com.ice.studyroom.domain.admin.presentation.dto.response;

import java.time.LocalDateTime;

import com.ice.studyroom.domain.penalty.domain.type.PenaltyReasonType;
import com.ice.studyroom.domain.penalty.domain.type.PenaltyStatus;

public record AdminPenaltyRecordResponse(
	String userName,
	String studentNum,
	PenaltyReasonType reason,
	PenaltyStatus status,
	LocalDateTime penaltyStart,
	LocalDateTime penaltyEnd
) {
	public static AdminPenaltyRecordResponse of(String userName, String studentNum,
		PenaltyReasonType reason, PenaltyStatus status, LocalDateTime penaltyStart, LocalDateTime penaltyEnd) {
		return new AdminPenaltyRecordResponse(userName, studentNum, reason, status, penaltyStart, penaltyEnd);
	}
}
