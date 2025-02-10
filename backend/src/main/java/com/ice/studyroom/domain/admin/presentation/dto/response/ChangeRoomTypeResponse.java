package com.ice.studyroom.domain.admin.presentation.dto.response;

import com.ice.studyroom.domain.admin.domain.type.RoomType;

public record ChangeRoomTypeResponse(long id, RoomType roomType) {
}
