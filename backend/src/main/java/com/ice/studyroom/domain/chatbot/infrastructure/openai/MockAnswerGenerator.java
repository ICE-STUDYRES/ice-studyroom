package com.ice.studyroom.domain.chatbot.infrastructure.openai;

import com.ice.studyroom.domain.chatbot.domain.service.AnswerGenerator;
import com.ice.studyroom.domain.chatbot.presentation.dto.response.AnswerResponse;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Primary;
import java.util.*;

@Component
@Primary
public class MockAnswerGenerator implements AnswerGenerator {

	@Override
	public AnswerResponse generate(String category, Long questionId){
		// 임시 더미 데이터 반환
		return new AnswerResponse(
			category,
			questionId,
			"예약은 하루 전까지 가능합니다. (Mock)",
			new AnswerResponse.Evidence(List.of("근거1","근거2")),
			new AnswerResponse.Links(
				null,
				null
			)
		);
	}
}
