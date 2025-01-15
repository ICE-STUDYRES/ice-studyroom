package com.ice.studyroom.domain.membership.presentation.dto.response;

import com.ice.studyroom.domain.identity.domain.JwtToken;

public record MemberLoginResponse(
	String accessToken,
	String refreshToken
) {
	// factory 메서드를 통한 생성
	public static MemberLoginResponse of(JwtToken jwtToken) {
		return new MemberLoginResponse(
			jwtToken.getAccessToken(),
			jwtToken.getRefreshToken()
		);
	}
}
