package com.ice.studyroom.domain.membership.domain.util;

import java.util.Arrays;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MembershipLogUtil {
	public static void log(String message, Object... details) {
		log.info("[MEMBERSHIP] {} - {}", message, Arrays.toString(details));
	}

	public static void logWarn(String message, Object... details) {
		log.warn("[MEMBERSHIP] {} - {}", message, Arrays.toString(details));
	}
}
