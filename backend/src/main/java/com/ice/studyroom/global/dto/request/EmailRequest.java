package com.ice.studyroom.global.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {
	@NotNull
	private String to;

	@NotNull
	private String subject;

	@NotNull
	private String body;
}
