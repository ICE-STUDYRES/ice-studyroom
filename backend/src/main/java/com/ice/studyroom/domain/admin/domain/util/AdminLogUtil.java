package com.ice.studyroom.domain.admin.domain.util;

import java.util.Arrays;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AdminLogUtil {

	public static void log(String message, Object... details) {
		log.info("[ADMIN] {} - {}", message, Arrays.toString(details));
	}

	public static void logWarn(String message, Object... details) {
		log.warn("[ADMIN] {} - {}", message, Arrays.toString(details));
	}
}
