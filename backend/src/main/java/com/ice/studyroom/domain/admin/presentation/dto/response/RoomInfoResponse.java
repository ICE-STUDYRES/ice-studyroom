package com.ice.studyroom.domain.admin.presentation.dto.response;

import com.ice.studyroom.domain.admin.domain.type.RoomType;

public record RoomInfoResponse(String roomNumber, RoomType roomType, int capacity) {}

