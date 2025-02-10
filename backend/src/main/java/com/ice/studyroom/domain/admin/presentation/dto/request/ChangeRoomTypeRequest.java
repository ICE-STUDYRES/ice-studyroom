package com.ice.studyroom.domain.admin.presentation.dto.request;

import com.ice.studyroom.domain.admin.domain.type.RoomType;

public record ChangeRoomTypeRequest(long id, RoomType type) {
}
