package com.ice.studyroom.domain.reservation.domain.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.ice.studyroom.domain.reservation.domain.type.ScheduleStatus;
import com.ice.studyroom.domain.reservation.dto.request.CreateScheduleRequest;
import com.ice.studyroom.domain.reservation.dto.response.ScheduleResponse;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "schedule")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Schedule {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private LocalDate scheduleDate;

	@Column(name = "room_id", nullable = false)
	private Long roomId;

	@Column(nullable = false)
	private String roomNumber;

	@Column(name = "time_slot_id", nullable = false)
	private Long timeSlotId;

	@Column(nullable = false)
	private LocalTime startTime;

	@Column(nullable = false)
	private LocalTime endTime;

	@Column(nullable = false)
	private Integer capacity;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Builder.Default
	private ScheduleStatus status = ScheduleStatus.AVAILABLE;

	@Column(nullable = false, updatable = false)
	@Builder.Default
	private LocalDateTime createdAt = LocalDateTime.now();

	@Column(nullable = false)
	@Builder.Default
	private LocalDateTime updatedAt = LocalDateTime.now();

	@OneToMany
	@JoinColumn(name = "schedule_id")
	@Builder.Default
	private List<Reservation> reservations = new ArrayList<>();

	public boolean isAvailable() {
		return status == ScheduleStatus.AVAILABLE;
	}

	public void reserve() {
		this.status = ScheduleStatus.RESERVED;
	}

	public void available() {
		this.status = ScheduleStatus.AVAILABLE;
	}

	public ScheduleResponse toResponse() {
		return ScheduleResponse.builder()
			.id(id)
			.scheduleDate(scheduleDate)
			.roomNumber(roomNumber)
			.startTime(startTime)
			.endTime(endTime)
			.capacity(capacity)
			.status(status)
			.createdAt(createdAt)
			.updatedAt(updatedAt)
			.build();
	}

	public static Schedule from(CreateScheduleRequest request) {
		return Schedule.builder()
			.scheduleDate(request.getScheduleDate())
			.roomNumber(request.getRoomNumber())
			.startTime(request.getStartTime())
			.endTime(request.getEndTime())
			.capacity(request.getCapacity())
			.build();
	}
}
