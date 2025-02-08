package com.ice.studyroom.domain.reservation.presentation.dto.response;

import java.util.List;

import com.ice.studyroom.domain.membership.domain.entity.Member;
import com.ice.studyroom.domain.reservation.domain.entity.Reservation;

import lombok.Builder;
import lombok.Getter;

public record GetReservationsResponse(
	Reservation reservation,
	List<Participant> participants
) {
	public static GetReservationsResponse from(Reservation reservation, List<Participant> participants) {
		return new GetReservationsResponse(reservation, participants);
	}

	@Getter
	@Builder
	public static class Participant {
		private String studentNum;
		private String name;

		public static Participant from(Member member) {
			return Participant.builder()
				.studentNum(member.getStudentNum())
				.name(member.getName())
				.build();
		}
	}

}
