package com.ice.studyroom.domain.reservation.presentation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ice.studyroom.domain.reservation.presentation.dto.request.CreateReservationRequest;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReservationControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	@DisplayName("예약 생성 성공 테스트")
	void createReservation_Success() throws Exception {
		// given
		CreateReservationRequest request = CreateReservationRequest.builder()
			.scheduleId(new Long[] {1L, 9L})
			.userName("도성현")
			.roomNumber("409-2")
			.startTime(LocalTime.of(9, 0))
			.endTime(LocalTime.of(11, 0))
			.build();

		// when & then
		mockMvc.perform(
				post("/api/reservations")
					.contentType(MediaType.APPLICATION_JSON)
					.characterEncoding("UTF-8")
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.message").value("예약이 성공적으로 완료되었습니다."))
			.andExpect(jsonPath("$.reservation.roomNumber").value("409-2"))
			.andExpect(jsonPath("$.reservation.startTime").value("09:00:00"))
			.andExpect(jsonPath("$.reservation.endTime").value("11:00:00"));
	}

	// @Test
	// @DisplayName("예약 생성 실패 - 잘못된 입력값")
	// void createReservation_Failure_InvalidInput() throws Exception {
	// 	// given
	// 	CreateReservationRequest request = CreateReservationRequest.builder()
	// 		.userId(1L)
	// 		.scheduleId(new Long[] {1L, 17L})
	// 		.userName("도성현")
	// 		.roomNumber("409-2")
	// 		.startTime(LocalTime.of(9, 0))
	// 		.endTime(LocalTime.of(11, 0))
	// 		.build();
	//
	// 	// when & then
	// 	mockMvc.perform(
	// 			post("/api/reservations")
	// 				.contentType(MediaType.APPLICATION_JSON)
	// 				.characterEncoding("UTF-8")
	// 				.content(objectMapper.writeValueAsString(request)))
	// 		.andExpect(status().isBadRequest());
	// }
}
