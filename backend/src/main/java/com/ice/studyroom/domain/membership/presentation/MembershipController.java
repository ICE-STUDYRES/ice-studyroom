package com.ice.studyroom.domain.membership.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ice.studyroom.domain.identity.infrastructure.security.JwtTokenProvider;
import com.ice.studyroom.domain.membership.application.MembershipService;
import com.ice.studyroom.domain.membership.presentation.dto.request.MemberCreateRequest;
import com.ice.studyroom.domain.membership.presentation.dto.request.MemberLoginRequest;
import com.ice.studyroom.domain.membership.presentation.dto.response.MemberCreateResponse;
import com.ice.studyroom.domain.membership.presentation.dto.response.MemberLoginResponse;
import com.ice.studyroom.global.dto.response.ResponseDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class MembershipController {
	private final MembershipService membershipService;
	private final JwtTokenProvider jwtTokenProvider;
	private final AuthenticationManager authenticationManager;

	@PostMapping
	public ResponseEntity<ResponseDto<MemberCreateResponse>> createUser(
		@Valid @RequestBody MemberCreateRequest request) {
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(ResponseDto.of(membershipService.createMember(request)));
	}

	@PostMapping("/login")
	public ResponseEntity<ResponseDto<MemberLoginResponse>> login(@Valid @RequestBody MemberLoginRequest request) {
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(ResponseDto.of(membershipService.login(request)));
	}
}
