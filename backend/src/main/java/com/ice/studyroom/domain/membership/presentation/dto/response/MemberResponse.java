package com.ice.studyroom.domain.membership.presentation.dto.response;

public record MemberResponse(
	String message
) {
	public static MemberResponse of(String message) {
		return new MemberResponse(message);
	}
}
