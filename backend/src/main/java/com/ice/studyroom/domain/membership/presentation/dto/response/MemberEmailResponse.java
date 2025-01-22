package com.ice.studyroom.domain.membership.presentation.dto.response;

public record MemberEmailResponse(
	String message
) {
	public static MemberEmailResponse of(String message) {
		return new MemberEmailResponse(message);
	}
}
