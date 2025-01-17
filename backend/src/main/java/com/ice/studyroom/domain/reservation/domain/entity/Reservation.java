package com.ice.studyroom.domain.reservation.domain.entity;

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

	// 예약 상태 변경 관련 메서드(checkIn(), checkOut(), cancel())
	public Reservation checkIn() {
		// validateCheckIn();
		return Reservation.builder()
			.id(this.id)
			.firstScheduleId(this.firstScheduleId)
			.secondScheduleId(this.secondScheduleId)
			.userEmail(this.userEmail)
			.userName(this.userName)
			.scheduleDate(this.scheduleDate)
			.roomNumber(this.roomNumber)
			.startTime(this.startTime)
			.endTime(this.endTime)
			.status(ReservationStatus.CHECKED_IN)
			.enterTime(LocalDateTime.now())
			.exitTime(this.exitTime)
			.createdAt(this.createdAt)
			.updatedAt(LocalDateTime.now())
			.build();
	}

	public Reservation checkOut() {
		// validateCheckOut();
		return Reservation.builder()
			.id(this.id)
			.firstScheduleId(this.firstScheduleId)
			.secondScheduleId(this.secondScheduleId).userEmail(this.userEmail)
			.userName(this.userName)
			.scheduleDate(this.scheduleDate)
			.roomNumber(this.roomNumber)
			.startTime(this.startTime)
			.endTime(this.endTime)
			.status(ReservationStatus.COMPLETED)
			.enterTime(this.enterTime)
			.exitTime(LocalDateTime.now())
			.createdAt(this.createdAt)
			.updatedAt(LocalDateTime.now())
			.build();
	}

	public Reservation cancel() {
		// validateCancellation();
		return Reservation.builder()
			.id(this.id)
			.firstScheduleId(this.firstScheduleId)
			.secondScheduleId(this.secondScheduleId).userEmail(this.userEmail)
			.userName(this.userName)
			.scheduleDate(this.scheduleDate)
			.roomNumber(this.roomNumber)
			.startTime(this.startTime)
			.endTime(this.endTime)
			.status(ReservationStatus.RESERVED)
			.enterTime(this.enterTime)
			.exitTime(this.exitTime)
			.createdAt(this.createdAt)
			.updatedAt(LocalDateTime.now())
			.build();
	}

	public static Reservation from(List<Schedule> schedules, CreateReservationRequest request, String email) {
		Schedule firstSchedule = schedules.get(0);
		Schedule secondSchedule = schedules.size() > 1 ? schedules.get(1) : null;
		System.out.println("Reservation.from() ID =" + firstSchedule.getId());
		System.out.println("Reservation.from() =" + firstSchedule.getStartTime());
		return Reservation.builder()
			.firstScheduleId(firstSchedule.getId())
			.secondScheduleId(secondSchedule != null ? secondSchedule.getId() : null)
			.userEmail(email)
			.userName(request.getUserName())
			.scheduleDate(firstSchedule.getScheduleDate())
			.roomNumber(firstSchedule.getRoomNumber())
			.startTime(firstSchedule.getStartTime())
			.endTime(secondSchedule != null ? secondSchedule.getEndTime() : firstSchedule.getEndTime())
			.status(ReservationStatus.RESERVED)
			.build();
	}
}
