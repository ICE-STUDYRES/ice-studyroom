package com.ice.studyroom.domain.ranking.application.event;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Component
public class EventIdGenerator {

	public String generate(String periodKey) {

		String date = LocalDate.now().toString(); // yyyy-MM-dd
		String shortUuid = UUID.randomUUID()
			.toString()
			.replace("-", "")
			.substring(0, 4);

		return periodKey.toLowerCase()
			+ "-"
			+ date
			+ "-uuid-"
			+ shortUuid;
	}
}
