package com.ice.studyroom.domain.membership.presentation.dto.response;

public record MemberLookupResponse(
	String email,
	String name
) {
	public static MemberLookupResponse of(String email, String name){
		return new MemberLookupResponse(email, name);
	}
}
