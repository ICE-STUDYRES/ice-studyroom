package com.ice.studyroom.domain.membership.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ice.studyroom.domain.membership.application.MembershipService;
import com.ice.studyroom.domain.membership.presentation.dto.request.MemberCreateRequest;
import com.ice.studyroom.domain.membership.presentation.dto.request.EmailVerificationRequest;
import com.ice.studyroom.domain.membership.presentation.dto.request.MemberEmailVerificationRequest;
import com.ice.studyroom.domain.membership.presentation.dto.request.MemberLoginRequest;
import com.ice.studyroom.domain.membership.presentation.dto.request.TokenRequest;
import com.ice.studyroom.domain.membership.presentation.dto.response.MemberEmailResponse;
import com.ice.studyroom.domain.membership.presentation.dto.response.MemberLoginResponse;
import com.ice.studyroom.domain.membership.presentation.dto.response.MemberResponse;
import com.ice.studyroom.global.dto.response.ResponseDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class MembershipController {
	private final MembershipService membershipService;

	@PostMapping
	public ResponseEntity<ResponseDto<MemberResponse>> createUser(
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

	@PostMapping("/refresh")
	public ResponseEntity<ResponseDto<MemberLoginResponse>> refresh(
		@RequestHeader("Authorization") String authorizationHeader,
		@Valid @RequestBody TokenRequest request) {
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(ResponseDto.of(membershipService.refresh(authorizationHeader, request)));
	}

	@PostMapping("/logout")
	public ResponseEntity<ResponseDto<MemberResponse>> logout(
		@RequestHeader("Authorization") String authorizationHeader,
		@Valid @RequestBody TokenRequest request) {
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(ResponseDto.of(membershipService.logout(authorizationHeader, request)));
	}

	@PostMapping("/email-verification")
	public ResponseEntity<ResponseDto<MemberEmailResponse>> sendEmail(
		@Valid @RequestBody EmailVerificationRequest request) {
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(ResponseDto.of(membershipService.sendMail(request)));
	}

	@PostMapping("/email-verification/confirm")
	public ResponseEntity<ResponseDto<MemberEmailResponse>> checkEmailVerification(
		@Valid @RequestBody MemberEmailVerificationRequest request) {
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(ResponseDto.of(membershipService.checkEmailVerification(request)));
	}
}
