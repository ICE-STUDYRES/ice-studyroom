package com.ice.studyroom.domain.ranking.application.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RankingQueryServiceTest {

	private final RankingQueryService service =
		new RankingQueryService(null, null);

	@Test
	@DisplayName("1글자 이름은 * 처리된다")
	void maskName_oneChar() {
		String result = service.maskName("김");
		assertThat(result).isEqualTo("*");
	}

	@Test
	@DisplayName("2글자 이름은 첫글자만 노출된다")
	void maskName_twoChar() {
		String result = service.maskName("김철");
		assertThat(result).isEqualTo("김*");
	}

	@Test
	@DisplayName("3글자 이름은 가운데가 마스킹된다")
	void maskName_threeChar() {
		String result = service.maskName("김예준");
		assertThat(result).isEqualTo("김*준");
	}

	@Test
	@DisplayName("4글자 이름은 중간 두글자가 마스킹된다")
	void maskName_fourChar() {
		String result = service.maskName("김예준짱");
		assertThat(result).isEqualTo("김**짱");
	}

	@Test
	@DisplayName("5글자 이상도 length-2 만큼 마스킹된다")
	void maskName_longName() {
		String result = service.maskName("김예준최고야");
		assertThat(result).isEqualTo("김****야");
	}

	@Test
	@DisplayName("공백은 trim 처리된다")
	void maskName_trim() {
		String result = service.maskName("  김예준  ");
		assertThat(result).isEqualTo("김*준");
	}

	@Test
	@DisplayName("null은 빈 문자열 반환")
	void maskName_null() {
		String result = service.maskName(null);
		assertThat(result).isEqualTo("");
	}

	@Test
	@DisplayName("빈 문자열은 빈 문자열 반환")
	void maskName_blank() {
		String result = service.maskName("   ");
		assertThat(result).isEqualTo("");
	}
}
