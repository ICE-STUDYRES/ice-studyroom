package com.ice.studyroom.domain.schedule.domain.service;

import com.ice.studyroom.domain.reservation.domain.exception.reservation.ReservationScheduleNotFoundException;
import com.ice.studyroom.domain.reservation.domain.exception.type.reservation.ReservationActionType;
import com.ice.studyroom.domain.reservation.domain.exception.type.reservation.ScheduleNotFoundReason;
import com.ice.studyroom.domain.reservation.infrastructure.kafka.VacancyNotificationProducer;
import com.ice.studyroom.domain.reservation.infrastructure.persistence.ScheduleRepository;
import com.ice.studyroom.domain.schedule.domain.entity.Schedule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ScheduleCanceller {
	private final ScheduleRepository scheduleRepository;
	private final VacancyNotificationProducer vacancyNotificationProducer;

	@Transactional
	public void cancelAssociatedSchedules(Long reservationId, Long firstScheduleId, Optional<Long> secondScheduleIdOpt) {
		findAndCancelSchedule(reservationId, firstScheduleId);
		secondScheduleIdOpt.ifPresent(id -> findAndCancelSchedule(reservationId, id));
	}

	private void findAndCancelSchedule(Long reservationId, Long scheduleId) {
		Schedule schedule = scheduleRepository.findById(scheduleId)
			.orElseThrow(() -> new ReservationScheduleNotFoundException(
				ScheduleNotFoundReason.NOT_FOUND, scheduleId, reservationId, ReservationActionType.CANCEL_RESERVATION
			));
		schedule.cancel();
		// 배포할 경우 주석 제거
//		if (schedule.getCapacity() - schedule.getCurrentRes() == 1) {
//			vacancyNotificationProducer.sendVacancyNotificationToSubscribers(scheduleId, schedule.getRoomNumber());
//		}
	}
}
