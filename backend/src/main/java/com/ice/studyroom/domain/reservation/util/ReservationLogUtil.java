package com.ice.studyroom.domain.reservation.util;

import java.util.Arrays;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReservationLogUtil {

	public static void log(String message, Object... details) {
		log.info("[RESERVATION] {} - {}", message, Arrays.toString(details));
	}

	public static void logWarn(String message, Object... details) {
		log.warn("[RESERVATION] {} - {}", message, Arrays.toString(details));
	}

	public static void logError(String message, Object... details) {
		log.warn("[RESERVATION] {} - {}", message, Arrays.toString(details));
	}
}
