package com.ice.studyroom.domain.membership.presentation.dto.response;

import java.time.LocalDateTime;

import com.ice.studyroom.domain.penalty.domain.type.PenaltyReasonType;

public record MemberLookupResponse(
	String email,
	String name,
	PenaltyReasonType penaltyReasonType,
	LocalDateTime penaltyEndAt
) {
	public static MemberLookupResponse of(String email, String name, PenaltyReasonType penaltyReasonType,
		LocalDateTime penaltyEndAt) {

		return new MemberLookupResponse(email, name, penaltyReasonType, penaltyEndAt);
	}

	public static MemberLookupResponse of(String email, String userName) {
		return new MemberLookupResponse(email, userName, null, null);
	}
}
