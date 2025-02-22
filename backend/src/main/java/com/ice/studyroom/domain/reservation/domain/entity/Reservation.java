package com.ice.studyroom.domain.reservation.domain.entity;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import com.ice.studyroom.domain.reservation.domain.type.ReservationStatus;
import com.ice.studyroom.domain.reservation.presentation.dto.request.CreateReservationRequest;
import com.ice.studyroom.global.exception.BusinessException;
import com.ice.studyroom.global.type.StatusCode;

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
import lombok.Setter;

@Entity
@Table(name = "reservation")
@Getter
@Setter
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

	public boolean matchEmail(String email) { return userEmail.equals(email); }

	// 정상 입실인지 지각인지 노쇼인지 판단하는 코드
	public ReservationStatus checkAttendanceStatus(LocalDateTime now) {
		LocalDateTime startDateTime = LocalDateTime.of(scheduleDate, startTime);
		LocalDateTime endDateTime = LocalDateTime.of(scheduleDate, endTime);

		long minutesDifference = Duration.between(startDateTime, now).toMinutes();
		long minutesDurationOfReservation = Duration.between(startDateTime, endDateTime).toMinutes();

		if (now.isBefore(startDateTime)) {
			return ReservationStatus.RESERVED;
		} else if (minutesDifference <= 30) {
			return ReservationStatus.ENTRANCE;
		} else if (minutesDifference <= minutesDurationOfReservation) {
			return ReservationStatus.LATE;
		} else {
			return ReservationStatus.NO_SHOW;
		}
	}

	public void markStatus(ReservationStatus status) {
		this.status = status;
		this.updatedAt = LocalDateTime.now();
		if(status != ReservationStatus.CANCELLED && status != ReservationStatus.NO_SHOW) {
			this.enterTime = LocalDateTime.now();
		}
	}

	public void extendReservation(Long secondScheduleId, LocalTime endTime) {
		this.secondScheduleId = secondScheduleId;
		this.endTime = endTime;
		this.updatedAt = LocalDateTime.now();
	}

	public static Reservation from(List<Schedule> schedules, String email, String userName) {
		if (schedules == null || schedules.isEmpty()) {
			throw new BusinessException(StatusCode.BAD_REQUEST, "Schedules List가 비어있습니다.");
		}

		Schedule firstSchedule = schedules.get(0);
		Schedule secondSchedule = schedules.size() > 1 ? schedules.get(1) : null;

		if (firstSchedule.getRoomNumber() == null) {
			throw new BusinessException(StatusCode.BAD_REQUEST, "firstSchedule은 null일  수 없습니다.");
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
