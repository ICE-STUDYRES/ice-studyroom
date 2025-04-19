package com.ice.studyroom.domain.reservation.util;

import java.util.Arrays;
import java.util.List;

import com.ice.studyroom.domain.admin.domain.type.RoomType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReservationLogUtil {

	public static void logReservationRequest(RoomType type, Long[] scheduleIds, String[] participantEmails) {
		log.info("[RESERVATION] {} 예약 생성 요청 - scheduleIds: {} / participantEmails: {} ", getRoomTypeString(type),
			scheduleIds, participantEmails);
	}

	public static void logReservationSuccess(RoomType type, Long reservationId) {
		log.info("[RESERVATION] {} 예약 생성 성공 - reservationId: {}", getRoomTypeString(type), reservationId);
	}

	public static void logReservationFailure(RoomType type, String reason, Object... args) {
		log.warn("[RESERVATION] {} 예약 실패 - 이유 : {} / {}", getRoomTypeString(type), reason, Arrays.toString(args));
	}

	private static String getRoomTypeString(RoomType type) {
		return switch (type) {
			case GROUP -> "그룹";
			case INDIVIDUAL -> "개인";
		};
	}
}
