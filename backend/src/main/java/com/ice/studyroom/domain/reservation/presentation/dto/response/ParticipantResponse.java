package com.ice.studyroom.domain.reservation.presentation.dto.response;

import com.ice.studyroom.domain.membership.domain.entity.Member;

public record ParticipantResponse(String studentNum, String name) {
	public static ParticipantResponse from(Member member) {
		return new ParticipantResponse(member.getStudentNum(), member.getName());
	}
}

