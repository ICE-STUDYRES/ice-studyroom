package com.ice.studyroom.domain.reservation.domain.type;

public enum ReservationStatus {

	RESERVED,    // 예약됨
	ENTRANCE,    // 입장완료
	LATE,        // 지각
	NO_SHOW,     // 노쇼
	EXIT,        // 퇴장완료
	COMPLETED,   // 정상퇴실 확인
	CANCELLED    // 취소됨
}
