package com.ice.studyroom.domain.admin.domain.service.policy;

import com.ice.studyroom.domain.admin.domain.type.RoomType;
import org.springframework.stereotype.Service;

@Service
public class RoomPolicyService {

	public int getMinResByRoomType(RoomType roomType) {
		return switch (roomType) {
			case INDIVIDUAL -> 1;
			case GROUP -> 2;
		};
	}
}
