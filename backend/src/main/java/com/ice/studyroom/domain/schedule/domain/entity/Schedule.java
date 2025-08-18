package com.ice.studyroom.domain.schedule.domain.entity;

import java.time.LocalDate;
import java.time.LocalTime;

import com.ice.studyroom.domain.admin.domain.type.DayOfWeekStatus;
import com.ice.studyroom.domain.admin.domain.type.RoomType;
import com.ice.studyroom.domain.schedule.domain.exception.schedule.GroupRoomOnlyException;
import com.ice.studyroom.domain.schedule.domain.exception.schedule.ReservationCapacityExceededException;
import com.ice.studyroom.domain.schedule.domain.exception.schedule.CancellationNotAllowedException;
import com.ice.studyroom.domain.reservation.domain.type.ScheduleSlotStatus;
import com.ice.studyroom.global.entity.BaseTimeEntity;

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
@Table(name = "schedule")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Schedule extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(name = "room_type", nullable = false)
	@Builder.Default
	private RoomType roomType = RoomType.GROUP;

	@Column(name = "schedule_date", nullable = false)
	private LocalDate scheduleDate;

	@Column(name = "room_number", nullable = false, length = 20)
	private String roomNumber;

	@Column(name = "room_time_slot_id", nullable = false)
	private Long roomTimeSlotId;

	@Column(name = "start_time", nullable = false)
	private LocalTime startTime;

	@Column(name = "end_time",nullable = false)
	private LocalTime endTime;

	@Column(name = "current_res", nullable = false)
	@Builder.Default
	private Integer currentRes = 0;

	@Column(name = "capacity", nullable = false)
	private Integer capacity;

	@Column(name = "min_res", nullable = false)
	private Integer minRes;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	@Builder.Default
	private ScheduleSlotStatus status = ScheduleSlotStatus.AVAILABLE;

	@Enumerated(EnumType.STRING)
	@Column(name = "day_of_week", nullable = false)
	private DayOfWeekStatus dayOfWeek;

	public boolean isAvailable() {
		return status == ScheduleSlotStatus.AVAILABLE;
	}

	public boolean isCurrentResLessThanCapacity() {
		return currentRes < capacity;
	}

	public void updateStatus(ScheduleSlotStatus newStatus) {
		this.status = newStatus;
	}

	public void updateGroupCurrentRes(int totalParticipants) {
		this.currentRes = totalParticipants;
	}

	public void reserve() {
		if (!isCurrentResLessThanCapacity()) {
			throw new ReservationCapacityExceededException("예약 가능한 자리가 없습니다. 스케줄 ID: " + this.id);
		}

		this.currentRes++;
		updateStatusIfFull();
	}

	public void cancel() {
		if (this.currentRes <= 0) {
			throw new CancellationNotAllowedException("취소할 예약이 없습니다. 스케줄 ID: " + this.id);
		}

		this.currentRes--;
		updateStatusIfAvailable();
	}

	public void validateForIndividualReservation() {
		if (this.roomType == RoomType.GROUP) {
			throw new GroupRoomOnlyException("해당 방은 단체예약 전용입니다. 스케줄 ID: " + this.id);
		}
	}

	private void updateStatusIfFull() {
		if (this.currentRes.equals(this.capacity)) {
			this.status = ScheduleSlotStatus.RESERVED;
		}
	}

	private void updateStatusIfAvailable() {
		if (this.currentRes < this.capacity) {
			this.status = ScheduleSlotStatus.AVAILABLE;
		}
	}
}
