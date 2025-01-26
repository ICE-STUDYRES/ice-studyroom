package com.ice.studyroom.domain.admin.application;

import com.ice.studyroom.domain.admin.presentation.dto.request.AdminCreateOccupyRequest;
import com.ice.studyroom.domain.admin.presentation.dto.response.AdminCreateOccupyResponse;
import com.ice.studyroom.domain.admin.presentation.dto.response.AdminDeleteOccupyResponse;
import com.ice.studyroom.domain.room_timeslot.domain.entity.RoomTimeSlot;
import com.ice.studyroom.domain.room_timeslot.domain.type.RoomTimeSlotStatus;
import com.ice.studyroom.domain.room_timeslot.infrastructure.persistence.RoomTimeSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

	private final RoomTimeSlotRepository roomTimeSlotRepository;

//	public List<Integer> getOccupiedRoomId() {
//		return roomTimeSlotRepository.findByStatus();
//	}

	public AdminCreateOccupyResponse adminOccupyRoom (AdminCreateOccupyRequest request) {

		RoomTimeSlot roomTimeSlot = roomTimeSlotRepository.findById(request.roomTimeSlotId())
			.orElseThrow(() -> new IllegalStateException("해당 ID에 일치하는 RoomTimeSlot이 없습니다."));

		//상태를 RESERVED로 변경
		if (roomTimeSlot.getStatus() == RoomTimeSlotStatus.RESERVED) {
			throw new IllegalStateException("해당 시간에는 이미 선점되어있습니다.");
		}

		//방 상태 변경
		roomTimeSlot.updateStatus(RoomTimeSlotStatus.RESERVED);
		//변경 사항 저장
		roomTimeSlotRepository.save(roomTimeSlot);

		return AdminCreateOccupyResponse.of("관리자가 특정 요일, 방, 시간대를 선점했습니다.");
	}

	public List<Long> getReservedRoomIds() {
		// 선점된 방 엔티티만 가져오기
		List<RoomTimeSlot> reservedRoomTimeSlots = roomTimeSlotRepository.findByStatus(RoomTimeSlotStatus.RESERVED);

		//ID만 추출하여 반환
		return reservedRoomTimeSlots.stream().map(RoomTimeSlot::getId).toList();
	}

	public AdminDeleteOccupyResponse adminDeleteOccupy(AdminCreateOccupyRequest request) {

		RoomTimeSlot roomTimeSlot = roomTimeSlotRepository.findById(request.roomTimeSlotId())
			.orElseThrow(() -> new IllegalStateException("해당 ID에 일치하는 RoomTimeSlot이 없습니다."));

		if(roomTimeSlot.getStatus() == RoomTimeSlotStatus.AVAILABLE) {
			throw new IllegalStateException("해당 시간은 현재 사용가능 상태입니다.");
		}

		//방 상태 변경
		roomTimeSlot.updateStatus(RoomTimeSlotStatus.AVAILABLE);
		//변경 사항 저장
		roomTimeSlotRepository.save(roomTimeSlot);

		return AdminDeleteOccupyResponse.of("관리자가 특정 요일, 방, 시간대의 선점을 해지했습니다.");
	}
}
