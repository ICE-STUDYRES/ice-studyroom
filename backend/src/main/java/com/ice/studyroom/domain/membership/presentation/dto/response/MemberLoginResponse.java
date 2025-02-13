package com.ice.studyroom.domain.membership.presentation.dto.response;

import com.ice.studyroom.domain.identity.domain.JwtToken;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record MemberLoginResponse(
	@NotNull
	@Schema(description = "Access Token", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJnbGF4eXRAaHVmcy5hYy5rciIsImF1dGgiOiJST0xFX1VTRVIiLCJleHAiOjE3MzkyODc0MDR9.krKvoSNZBx3r7hrrym-WKslbQclORPwbkDEeWJeHMpw")
	String accessToken,
	@NotNull
	@Schema(description = "사용자 권한", example = "ROLE_USER")
	String role
) {

	public static MemberLoginResponse of(JwtToken jwtToken) {
		return new MemberLoginResponse(
			jwtToken.getAccessToken(),
			jwtToken.getRole()
		);
	}
}
