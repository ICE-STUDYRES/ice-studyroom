package com.ice.studyroom.domain.membership.presentation;

import java.time.Duration;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ice.studyroom.domain.identity.domain.JwtToken;
import com.ice.studyroom.domain.membership.application.MembershipService;
import com.ice.studyroom.domain.membership.presentation.dto.request.EmailVerificationRequest;
import com.ice.studyroom.domain.membership.presentation.dto.request.MemberCreateRequest;
import com.ice.studyroom.domain.membership.presentation.dto.request.MemberEmailVerificationRequest;
import com.ice.studyroom.domain.membership.presentation.dto.request.MemberLoginRequest;
import com.ice.studyroom.domain.membership.presentation.dto.request.UpdatePasswordRequest;
import com.ice.studyroom.domain.membership.presentation.dto.response.MemberEmailResponse;
import com.ice.studyroom.domain.membership.presentation.dto.response.MemberLoginResponse;
import com.ice.studyroom.domain.membership.presentation.dto.response.MemberLookupResponse;
import com.ice.studyroom.domain.membership.presentation.dto.response.MemberResponse;
import com.ice.studyroom.global.dto.response.ResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@Tag(
	name = "Member",
	description = "회원 가입, 로그인, 로그아웃, 인증 메일 전송 등의 기능을 제공합니다."
)
@RequiredArgsConstructor
public class MembershipController {
	private final MembershipService membershipService;

	@Operation(summary = "회원가입", description = "회원가입을 요청을 처리합니다.")
	@ApiResponse(responseCode = "200", description = "예약 정보 조회 성공")
	@ApiResponse(responseCode = "400", description = "이메일 인증을 완료하지 않음")
	@ApiResponse(responseCode = "400", description = "인증 코드가 유효하지 않거나 만료됨")
	@PostMapping
	public ResponseEntity<ResponseDto<MemberResponse>> createUser(
		@Valid @RequestBody MemberCreateRequest request) {
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(ResponseDto.of(membershipService.createMember(request)));
	}

	@Operation(summary = "유저 정보 조회", description = "토큰을 통해 유저의 정보를 조회합니다.")
	@ApiResponse(responseCode = "200", description = "유저 정보 조회 성공")
	@ApiResponse(responseCode = "500", description = "유저 정보 조회 실패")
	@GetMapping
	public ResponseEntity<ResponseDto<MemberLookupResponse>> getUser(
		@RequestHeader("Authorization") String authorizationHeader) {
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(ResponseDto.of(membershipService.lookUpMember(authorizationHeader)));
	}

	@Operation(summary = "로그인", description = "로그인 요청을 처리합니다.")
	@ApiResponse(responseCode = "200", description = "로그인 성공")
	@ApiResponse(responseCode = "403", description = "패널티가 적용 중인 유저일 경우")
	@ApiResponse(responseCode = "500", description = "로그인 실패")
	@PostMapping("/login")
	public ResponseEntity<ResponseDto<MemberLoginResponse>> login(
		@Valid @RequestBody MemberLoginRequest request,
		HttpServletResponse response) {
		JwtToken jwtToken = membershipService.login(request);

		ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh_token", jwtToken.getRefreshToken())
			.httpOnly(true)   // JavaScript에서 접근 불가 (XSS 방어)
			.secure(true)     // 배포 시 true로 변경 예정, HTTPS 환경에서만 사용 가능
			.sameSite("Strict") // CSRF 방어
			.path("/api/users/refresh") // 특정 경로에서만 접근 가능
			.maxAge(Duration.ofDays(7)) // 7일간 유지
			.build();
		response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

		return ResponseEntity
			.status(HttpStatus.OK)
			.body(ResponseDto.of(MemberLoginResponse.of(jwtToken)));
	}

	@Operation(summary = "토큰 재발급", description = "로그인 유지를 위한 토큰을 재발급해줍니다.")
	@ApiResponse(responseCode = "200", description = "토큰 발급 성공")
	@ApiResponse(responseCode = "500", description = "토큰 발급 실패")
	@PostMapping("/refresh")
	public ResponseEntity<ResponseDto<MemberLoginResponse>> refresh(
		@CookieValue(value = "refresh_token") String refreshToken,
		@RequestHeader("Authorization") String authorizationHeader) {

		JwtToken jwtToken = membershipService.refresh(authorizationHeader, refreshToken);

		ResponseCookie newRefreshTokenCookie = ResponseCookie.from("refresh_token", jwtToken.getRefreshToken())
			.httpOnly(true)
			.secure(true)
			.sameSite("Strict")
			.path("/api/users/refresh")
			.maxAge(Duration.ofDays(7))
			.build();

		return ResponseEntity
			.status(HttpStatus.OK)
			.header(HttpHeaders.SET_COOKIE, newRefreshTokenCookie.toString())
			.body(ResponseDto.of(MemberLoginResponse.of(jwtToken)));
	}

	@PostMapping("/logout")
	@Operation(summary = "로그아웃", description = "로그아웃 요청을 처리합니다.")
	@ApiResponse(responseCode = "200", description = "로그아웃 성공")
	@ApiResponse(responseCode = "500", description = "로그아웃 실패")
	public ResponseEntity<ResponseDto<MemberResponse>> logout(
		@CookieValue(value = "refresh_token") String refreshToken,
		@RequestHeader("Authorization") String authorizationHeader
		) {
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(ResponseDto.of(membershipService.logout(authorizationHeader, refreshToken)));
	}

	@Operation(summary = "비밀번호 변경", description = "비밀번호 변경 요청을 처리합니다.")
	@ApiResponse(responseCode = "200", description = "비밀번호 변경 성공")
	@ApiResponse(responseCode = "500", description = "비밀번호 변경 실패")
	@PatchMapping("/password")
	public ResponseEntity<ResponseDto<String>> updatePassword(
		@RequestHeader("Authorization") String authorizationHeader,
		@Valid @RequestBody UpdatePasswordRequest request) {
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(ResponseDto.of(membershipService.updatePassword(authorizationHeader, request)));
	}

	@Operation(summary = "이메일 인증 메일 전송", description = "이메일 인증 메일 전송 요청을 처리합니다.")
	@ApiResponse(responseCode = "200", description = "이메일 전송 성공")
	@ApiResponse(responseCode = "409", description = "이미 가입된 이메일일 경우")
	@ApiResponse(responseCode = "429", description = "중복으로 이메일 인증을 요청했을 경우")
	@ApiResponse(responseCode = "500", description = "이메일 발송 실패")
	@PostMapping("/email-verification")
	public ResponseEntity<ResponseDto<MemberEmailResponse>> sendEmail(
		@Valid @RequestBody EmailVerificationRequest request) {
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(ResponseDto.of(membershipService.sendMail(request)));
	}

	@Operation(summary = "이메일 인증 코드 검증", description = "사용자가 입력한 이메일 인증 코드를 확인합니다.")
	@ApiResponse(responseCode = "200", description = "이메일 인증 성공")
	@ApiResponse(responseCode = "401", description = "유효하지 않은 코드일 경우")
	@PostMapping("/email-verification/confirm")
	public ResponseEntity<ResponseDto<MemberEmailResponse>> checkEmailVerification(
		@Valid @RequestBody MemberEmailVerificationRequest request) {
		return ResponseEntity
			.status(HttpStatus.OK)
			.body(ResponseDto.of(membershipService.checkEmailVerification(request)));
	}
}
