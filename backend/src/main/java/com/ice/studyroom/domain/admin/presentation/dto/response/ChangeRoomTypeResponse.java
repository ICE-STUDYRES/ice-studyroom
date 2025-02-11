package com.ice.studyroom.domain.admin.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ice.studyroom.domain.admin.domain.type.RoomType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "방 타입 변경 응답 DTO")
public record ChangeRoomTypeResponse(

	@Schema(description = "변경된 방 ID", example = "2185")
	@JsonProperty("id")
	long id,

	@Schema(description = "변경된 방 타입", example = "GROUP")
	@JsonProperty("roomType")
	RoomType roomType
) {
}
