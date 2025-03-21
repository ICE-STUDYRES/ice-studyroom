package com.ice.studyroom.domain.admin.scheduler;

import java.util.List;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ice.studyroom.domain.admin.domain.entity.RoomTimeSlot;
import com.ice.studyroom.domain.admin.infrastructure.persistence.RoomTimeSlotRepository;
import com.ice.studyroom.domain.reservation.domain.type.ScheduleSlotStatus;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class RoomTimeSlotScheduler {

	private final RoomTimeSlotRepository roomTimeSlotRepository;

	@Transactional
	@Scheduled(cron = "0 0 0 * * 1")
	public void roomTimeSlotInitScheduler() {
		log.info("RoomTimeSlot init scheduler 가 동작합니다.");
		List<RoomTimeSlot> unavailableRoomTimeSlotList = roomTimeSlotRepository.findByStatus(ScheduleSlotStatus.UNAVAILABLE);
		unavailableRoomTimeSlotList.forEach(roomTimeSlot -> roomTimeSlot.updateStatus(ScheduleSlotStatus.AVAILABLE));
		log.info("RoomTimeSlot init scheduler 가 정상적으로 동작되었습니다.");
	}
}
