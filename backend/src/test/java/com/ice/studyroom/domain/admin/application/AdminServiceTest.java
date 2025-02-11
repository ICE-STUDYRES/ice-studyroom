package com.ice.studyroom.domain.admin.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ice.studyroom.domain.admin.domain.entity.RoomTimeSlot;
import com.ice.studyroom.domain.admin.domain.type.RoomType;
import com.ice.studyroom.domain.admin.infrastructure.persistence.RoomTimeSlotRepository;
import com.ice.studyroom.domain.admin.presentation.dto.request.ChangeRoomTypeRequest;
import com.ice.studyroom.global.exception.BusinessException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class AdminServiceTest {

	@Mock
	private RoomTimeSlotRepository roomTimeSlotRepository;

	@InjectMocks
	private AdminService adminService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void changeRoomType_Success() {
		// Given
		Long roomId = 1L;
		RoomType type = RoomType.GROUP;
		ChangeRoomTypeRequest request = new ChangeRoomTypeRequest(roomId, type);
		RoomTimeSlot roomTimeSlot = mock(RoomTimeSlot.class);
		when(roomTimeSlot.getId()).thenReturn(roomId);
		when(roomTimeSlot.getRoomType()).thenReturn(type);

		when(roomTimeSlotRepository.findById(roomId)).thenReturn(java.util.Optional.of(roomTimeSlot));

		// When
		adminService.changeRoomType(request);

		// Then
		assertEquals(type, roomTimeSlot.getRoomType());
		verify(roomTimeSlotRepository, times(1)).save(roomTimeSlot); // save()가 호출되었는지 확인
	}

	@Test
	void changeRoomType_RoomNotFound() {
		// Given
		Long roomId = 999L;
		RoomType type = RoomType.GROUP;
		ChangeRoomTypeRequest request = new ChangeRoomTypeRequest(roomId, type);

		when(roomTimeSlotRepository.findById(roomId)).thenReturn(java.util.Optional.empty());

		// When & Then (예외 발생 확인)
		assertThrows(BusinessException.class, () -> adminService.changeRoomType(request));
	}
}

