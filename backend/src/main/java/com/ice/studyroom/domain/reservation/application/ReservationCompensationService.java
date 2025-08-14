package com.ice.studyroom.domain.reservation.application;

import com.ice.studyroom.domain.schedule.domain.entity.Schedule;
import com.ice.studyroom.domain.reservation.infrastructure.persistence.ScheduleRepository;
import com.ice.studyroom.domain.reservation.util.ReservationLogUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationCompensationService {

	private final ScheduleRepository scheduleRepository;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void rollbackSchedules(List<Long> scheduleIds, String email) {
		List<Long> sortedScheduleIds = scheduleIds.stream()
			.sorted()
			.collect(Collectors.toList());

		List<Schedule> schedules = scheduleRepository.findByIdsWithPessimisticLock(sortedScheduleIds);

		schedules.forEach(Schedule::cancel);

		scheduleRepository.saveAll(schedules);
		ReservationLogUtil.log("보상 트랜잭션 수행 완료", "예약자: " + email, "롤백된 스케줄 ID: " + scheduleIds);
	}
}
