package com.ice.studyroom.domain.reservation.domain.entity;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import com.ice.studyroom.domain.reservation.domain.type.ReservationStatus;
import com.ice.studyroom.domain.reservation.presentation.dto.request.CreateReservationRequest;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reservation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Reservation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "first_schedule_id")
	private Long firstScheduleId;

	@Column(name = "second_schedule_id")
	private Long secondScheduleId;

	@Column(nullable = false)
	private String userEmail;

	@Column(nullable = false)
	private String userName;

	@Column(nullable = false)
	private LocalDate scheduleDate;

	@Column(nullable = false)
	private String roomNumber;

	@Column(nullable = false)
	private LocalTime startTime;

	@Column(nullable = false)
	private LocalTime endTime;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Builder.Default
	private ReservationStatus status = ReservationStatus.RESERVED;

	private LocalDateTime enterTime;

	private LocalDateTime exitTime;

	@Column(nullable = false, updatable = false)
	@Builder.Default
	private LocalDateTime createdAt = LocalDateTime.now();

	@Column(nullable = false)
	@Builder.Default
	private LocalDateTime updatedAt = LocalDateTime.now();

	public boolean isReserved() {
		return status == ReservationStatus.RESERVED;
	}

	// 정상 입실인지 지각인지 노쇼인지 판단하는 코드
	public ReservationStatus checkAttendanceStatus(LocalDateTime now) {
		LocalDateTime startDateTime = LocalDateTime.of(createdAt.toLocalDate(), startTime);
		LocalDateTime endDateTime = LocalDateTime.of(createdAt.toLocalDate(), endTime);

		long minutesDifference = Duration.between(startDateTime, now).toMinutes();
		long minutesDurationOfReservation = Duration.between(startDateTime, endDateTime).toMinutes();

		if (now.isBefore(startDateTime)) {
			return ReservationStatus.RESERVED;
		} else if (minutesDifference <= 30) {
			markStatus(ReservationStatus.ENTRANCE);
			return ReservationStatus.ENTRANCE;
		} else if (minutesDifference <= minutesDurationOfReservation) {
			markStatus(ReservationStatus.LATE);
			return ReservationStatus.LATE;
			// 패널티 추가로직
		} else {
			markStatus(ReservationStatus.NO_SHOW);
			return ReservationStatus.NO_SHOW;
		}
	}

	private void markStatus(ReservationStatus status) {
		this.status = status;
		this.enterTime = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}

	public static Reservation from(List<Schedule> schedules, String email, String userName) {
		if (schedules == null || schedules.isEmpty()) {
			throw new IllegalArgumentException("Schedules List가 비어있습니다.");
		}

		Schedule firstSchedule = schedules.get(0);
		Schedule secondSchedule = schedules.size() > 1 ? schedules.get(1) : null;

		if (firstSchedule.getRoomNumber() == null) {
			throw new IllegalArgumentException("firstSchedule은 null일  수 없습니다.");
		}

		return Reservation.builder()
			.firstScheduleId(firstSchedule.getId())
			.secondScheduleId(secondSchedule != null ? secondSchedule.getId() : null)
			.userEmail(email)
			.userName(userName)
			.scheduleDate(firstSchedule.getScheduleDate())
			.roomNumber(firstSchedule.getRoomNumber())
			.startTime(firstSchedule.getStartTime())
			.endTime(secondSchedule != null ? secondSchedule.getEndTime() : firstSchedule.getEndTime())
			.status(ReservationStatus.RESERVED)
			.build();
	}
}
