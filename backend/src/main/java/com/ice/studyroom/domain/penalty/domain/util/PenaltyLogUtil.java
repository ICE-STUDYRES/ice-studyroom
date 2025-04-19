package com.ice.studyroom.domain.penalty.domain.util;

import java.util.Arrays;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PenaltyLogUtil {
	public static void log(String message, Object... details) {
		log.info("[PENALTY] {} - {}", message, Arrays.toString(details));
	}

	public static void logWarn(String message, Object... details) {
		log.warn("[PENALTY] {} - {}", message, Arrays.toString(details));
	}
}
