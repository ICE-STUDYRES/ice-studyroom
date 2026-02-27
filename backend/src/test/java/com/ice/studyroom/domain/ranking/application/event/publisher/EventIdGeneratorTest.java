package com.ice.studyroom.domain.ranking.application.event.publisher;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EventIdGeneratorTest {

	private final EventIdGenerator generator = new EventIdGenerator();

	@Test
	void generate_format_is_correct() {
		String eventId = generator.generate("weekly-2026-02-27");

		assertThat(eventId)
			.startsWith("weekly-2026-02-27-uuid-");

		assertThat(eventId.length()).isGreaterThan(25);
	}
}
