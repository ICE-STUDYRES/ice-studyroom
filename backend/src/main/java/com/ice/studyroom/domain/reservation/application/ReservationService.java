package com.ice.studyroom.domain.reservation.application;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ice.studyroom.domain.identity.domain.service.TokenService;
import com.ice.studyroom.domain.reservation.domain.entity.Reservation;
import com.ice.studyroom.domain.reservation.domain.entity.Schedule;
import com.ice.studyroom.domain.reservation.infrastructure.persistence.ReservationRepository;
import com.ice.studyroom.domain.reservation.infrastructure.persistence.ScheduleRepository;
import com.ice.studyroom.domain.reservation.presentation.dto.request.CreateReservationRequest;
import com.ice.studyroom.domain.reservation.presentation.dto.request.DeleteReservationRequest;
import com.ice.studyroom.domain.reservation.presentation.dto.response.ReservationResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

	private final TokenService tokenService;
	private final ReservationRepository reservationRepository;
	private final ScheduleRepository scheduleRepository;

	public List<Reservation> getMyReservation(String authorization) {
		String email = tokenService.extractEmailFromAccessToken(authorization);
		return reservationRepository.findByUserEmail(email);
	}

	public List<Schedule> getSchedule() {
		LocalDate today = LocalDate.now();
		return scheduleRepository.findByScheduleDate(today);
	}

	@Transactional
	public ReservationResponse createReservation(String authorizationHeader, CreateReservationRequest request) {
		/*
		신청 시간이 1시간-2시간인지 유효성 검증
		 */
		request.validateScheduleIds();

		/*
		TODO: 사용자 관련 사항을 확인한 예정인 부분
		추후에는 JWT를 통한 사용자 정보를 토대로, 본인의 예약인지 확인하고 예약을 취소할 예정
		*/

		/*
		예약이 가능한 스케줄인지 확인
		 */
		List<Schedule> schedules = findSchedules(request.getScheduleId());
		validateSchedulesAvailable(schedules);

		String email = tokenService.extractEmailFromAccessToken(authorizationHeader);
		Reservation reservation = Reservation.from(schedules, request, email);

		/*
		연속된 스케줄인지 확인하는 로직,
		연속된 스케줄: 연속된 시간 & 같은 방
		자체 필드 내의 데이터 유효성 검증은 TDA 원칙 적용
		 */
		if (request.isConsecutiveReservation()) {
			// 연속 시간 혹은 같은 방이 아닐 경우 오류 발생
			validateConsecutiveSchedules(request);
		}

		schedules.forEach(schedule -> {
			schedule.reserve();
		});

		// 예약 생성 및 저장
		scheduleRepository.saveAll(schedules);
		reservationRepository.save(reservation);

		return ReservationResponse.of(reservation);
	}

	@Transactional
	public void cancelReservation(DeleteReservationRequest request) {
		// TODO: 추후에는 JWT를 통한 사용자 정보를 토대로, 본인의 예약인지 확인하고 예약을 취소할 예정

		// 예약 데이터를 가져온다.
		Reservation reservation = findReservationById(request.getReservationId());

		// 예약 상태 확인
		if (!reservation.isReserved()) {
			throw new IllegalStateException("이전에 예약이 되지 않았습니다.");
		}

		List<Schedule> schedules = new ArrayList<>();

		// first schedule은 항상 존재
		Schedule firstSchedule = scheduleRepository.findById(reservation.getFirstScheduleId())
			.orElseThrow(() -> new IllegalStateException("스케줄을 찾지 못했습니다."));
		firstSchedule.available();
		schedules.add(firstSchedule);

		// second schedule이 있는 경우 (2시간 예약인 경우)
		if (reservation.getSecondScheduleId() != null) {
			Schedule secondSchedule = scheduleRepository.findById(reservation.getSecondScheduleId())
				.orElseThrow(() -> new IllegalStateException("스케줄을 찾지 못했습니다."));
			secondSchedule.available();
			schedules.add(secondSchedule);
		}

		// TODO: 사용자 취소 횟수 차감 코드 구현 예정

		scheduleRepository.saveAll(schedules);
		reservationRepository.delete(reservation);
	}

	// TODO: 추후 Jpa에 종합할 예정
	private Reservation findReservationById(Long reservationId) {
		return reservationRepository.findById(reservationId)
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다."));
	}

	private List<Schedule> findSchedules(Long[] scheduleIds) {
		return Arrays.stream(scheduleIds)
			.map(id -> scheduleRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 스케줄입니다.")))
			.collect(Collectors.toList());
	}

	private void validateConsecutiveSchedules(CreateReservationRequest request) {
		Schedule firstSchedule = scheduleRepository.findById(request.getScheduleId()[0])
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 스케줄입니다."));
		Schedule secondSchedule = scheduleRepository.findById(request.getScheduleId()[1])
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 스케줄입니다."));

		// 같은 방인지 확인
		if (!firstSchedule.getRoomNumber().equals(secondSchedule.getRoomNumber())) {
			throw new IllegalArgumentException("연속된 예약은 같은 방에서만 가능합니다.");
		}

		// 시간이 연속되는지 확인
		if (!firstSchedule.getEndTime().equals(secondSchedule.getStartTime())) {
			throw new IllegalArgumentException("연속되지 않은 시간은 예약할 수 없습니다.");
		}
	}

	private void validateSchedulesAvailable(List<Schedule> schedules) {
		if (schedules.stream().anyMatch(schedule -> !schedule.isAvailable())) {
			throw new IllegalStateException(("이미 예약된 시간이 존재합니다."));
		}
	}
}
