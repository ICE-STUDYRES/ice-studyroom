package com.ice.studyroom.domain.admin.domain.service.manage;

import com.ice.studyroom.domain.admin.domain.service.policy.RoomPolicyService;
import com.ice.studyroom.domain.admin.domain.type.RoomType;
import com.ice.studyroom.domain.admin.domain.util.AdminLogUtil;
import com.ice.studyroom.domain.admin.infrastructure.persistence.RoomTimeSlotRepository;
import com.ice.studyroom.global.exception.jwt.RoomNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomModificationService {

	private final RoomTimeSlotRepository roomTimeSlotRepository;
	private final RoomPolicyService roomPolicyService;

	public void changeRoomType(String roomNumber, RoomType type) {
		int minRes = roomPolicyService.getMinResByRoomType(type);
		int updated = roomTimeSlotRepository.updateRoomTypeAndMinRes(roomNumber, type, minRes);
		if (updated == 0) {
			AdminLogUtil.logWarn("수정하려는 방의 정보를 찾을 수 없습니다.", "방 번호: " + roomNumber);
			throw new RoomNotFoundException(roomNumber);
		}
	}
}
