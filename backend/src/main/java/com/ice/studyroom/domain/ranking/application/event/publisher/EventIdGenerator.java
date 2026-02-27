package com.ice.studyroom.domain.ranking.application.event.publisher;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Component
public class EventIdGenerator {

	public String generate(String periodKey) {
		String shortUuid = UUID.randomUUID()
			.toString()
			.substring(0, 4);

		return periodKey + "-uuid-" + shortUuid;
	}
}
