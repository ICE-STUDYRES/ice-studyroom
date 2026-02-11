package com.ice.studyroom.domain.notification.presentation.controller;

import com.ice.studyroom.domain.notification.application.NotificationQueryService;
import com.ice.studyroom.domain.notification.presentation.dto.response.NotificationResponse;
import com.ice.studyroom.global.dto.response.ResponseDto;
import com.ice.studyroom.global.type.StatusCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

	private final NotificationQueryService notificationQueryService;

	@Operation(
		summary = "사용자 개별 안 읽은 알림 조회",
		description = "현재 로그인한 사용자의 안 읽은 알림 목록을 조회합니다."
	)
	@ApiResponse(responseCode = "200", description = "안 읽은 알림 조회 성공")
	@ApiResponse(responseCode = "500", description = "안 읽은 알림 조회 실패")
	@GetMapping("/unread")
	public ResponseEntity<ResponseDto<List<NotificationResponse>>> getUnreadNotifications(
		@RequestHeader("Authorization") String authorizationHeader
	) {
		return ResponseEntity
			.status(StatusCode.OK.getStatus())
			.body(
				ResponseDto.of(
					notificationQueryService.getUnreadNotifications(authorizationHeader),
					"안 읽은 알림 조회 성공"
				)
			);
	}

	@Operation(
		summary = "사용자 개별 알림 읽음 처리",
		description = "현재 로그인한 사용자의 알림을 단건으로 읽음 처리합니다."
	)
	@ApiResponse(responseCode = "200", description = "알림 읽음 처리 성공")
	@ApiResponse(responseCode = "404", description = "해당 알림을 찾을 수 없음")
	@ApiResponse(responseCode = "500", description = "알림 읽음 처리 실패")
	@PatchMapping("/{notificationId}/read")
	public ResponseEntity<ResponseDto<String>> readNotification(
		@PathVariable Long notificationId,
		@RequestHeader("Authorization") String authorizationHeader
	) {
		return ResponseEntity
			.status(StatusCode.OK.getStatus())
			.body(ResponseDto.of(
				notificationQueryService.readNotification(notificationId, authorizationHeader)
			));
	}

}
