package com.ice.studyroom.domain.reservation.application;

import com.ice.studyroom.domain.reservation.domain.entity.Schedule;
import com.ice.studyroom.domain.reservation.infrastructure.persistence.ReservationRepository;
import com.ice.studyroom.domain.reservation.infrastructure.persistence.ScheduleRepository;
import com.ice.studyroom.domain.reservation.util.ReservationLogUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReservationCompensationService {

	private final ScheduleRepository scheduleRepository;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void rollbackSchedules(List<Long> scheduleIds, String email) {
		List<Long> sortedScheduleIds = new ArrayList<>(scheduleIds);
		Collections.sort(sortedScheduleIds);

		List<Schedule> schedules = scheduleRepository.findByIdsWithPessimisticLock(sortedScheduleIds);

		for (Schedule schedule : schedules) {
			// 개인 예약을 위해 +1 했던 currentRes를 되돌림
			schedule.cancel();
		}

		scheduleRepository.saveAll(schedules);
		ReservationLogUtil.log("보상 트랜잭션 수행 완료", "예약자: " + email, "롤백된 스케줄 ID: " + scheduleIds);
	}
}
