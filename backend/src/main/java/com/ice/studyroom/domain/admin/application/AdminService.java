package com.ice.studyroom.domain.admin.application;

import com.ice.studyroom.domain.admin.presentation.dto.request.AdminCreateReserveRequest;
import com.ice.studyroom.domain.admin.presentation.dto.response.AdminCreateReserveResponse;
import com.ice.studyroom.domain.room_timeslot.domain.entity.RoomTimeSlot;
import com.ice.studyroom.domain.room_timeslot.domain.type.RoomTimeSlotStatus;
import com.ice.studyroom.domain.room_timeslot.infrastructure.persistence.RoomTimeSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

	private final RoomTimeSlotRepository roomTimeSlotRepository;

	public AdminCreateReserveResponse adminReserveRoom (AdminCreateReserveRequest request) {

		RoomTimeSlot roomTimeSlot = roomTimeSlotRepository.findById(request.roomTimeSlotId())
			.orElseThrow(() -> new IllegalStateException("해당 ID에 일치하는 RoomTimeSlot이 없습니다."));

		//상태를 UNAVAILABLE로 변경
		if (roomTimeSlot.getStatus() == RoomTimeSlotStatus.UNAVAILABLE) {
			throw new IllegalStateException("해당 시간에는 이미 예약되어있습니다.");
		}

		/*
		이후 버전 생각한거니 무시해주세요.
		 */
//		if (roomTimeSlot.getStatus() == RoomTimeSlotStatus.UNAVAILABLE) {
//			// 이미 예약된 경우 에러 메시지를 담은 Response 생성
//			return AdminCreateReserveResponse.of("이미 예약되어 있는 스케줄입니다.");
//		}

		//방 상태 변경
		roomTimeSlot.updateStatus(RoomTimeSlotStatus.UNAVAILABLE);
		//변경 사항 저장
		roomTimeSlotRepository.save(roomTimeSlot);

		return AdminCreateReserveResponse.of("관리자가 특정 요일, 방, 시간대를 예약했습니다.");
	}


}
