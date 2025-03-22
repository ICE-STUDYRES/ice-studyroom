package com.ice.studyroom.domain.reservation.presentation.dto.response;

import com.ice.studyroom.domain.membership.domain.entity.Member;

public record ParticipantResponse(String studentNum, String name, boolean isHolder) {
	public static ParticipantResponse from(Member member, boolean isHolder) {
		return new ParticipantResponse(member.getStudentNum(), member.getName(), isHolder);
	}
}

