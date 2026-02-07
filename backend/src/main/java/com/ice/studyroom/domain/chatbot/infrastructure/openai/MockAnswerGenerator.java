package com.ice.studyroom.domain.chatbot.infrastructure.openai;

import com.ice.studyroom.domain.chatbot.domain.service.AnswerGenerator;
import com.ice.studyroom.domain.chatbot.domain.type.CategoryType;
import com.ice.studyroom.domain.chatbot.presentation.dto.response.AnswerResponse;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Primary;
import java.util.*;

@Component
@Primary
public class MockAnswerGenerator implements AnswerGenerator {

	@Override
	public AnswerResponse generate(CategoryType category, Long questionId){
		// 임시 더미 데이터 반환
		return new AnswerResponse(
			category,
			questionId,
			"예약은 하루 전까지 가능합니다. (Mock)",
			new AnswerResponse.Evidence(List.of("근거1","근거2")),
			new AnswerResponse.Links(
				category.getRoute(),
				category.getNotionUrl()
			),
			new AnswerResponse.Support(
				"https://open.kakao.com/o/giOS427b",
				"김주희",
				"010-0000-0000"
			)
		);
	}
}
