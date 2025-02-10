package com.ice.studyroom.domain.admin.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ice.studyroom.domain.admin.domain.type.RoomType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record ChangeRoomTypeRequest(

	@NotNull
	@Schema(description = "방 ID", example = "2185", required = true)
	@JsonProperty("id")
	long id,

	@NotNull
	@Schema(description = "변경할 방 타입", example = "GROUP", required = true)
	@JsonProperty("type")
	RoomType type
) {
}
