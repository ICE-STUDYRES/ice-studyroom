package com.ice.studyroom.domain.room_timeslot.domain.entity;

import java.time.LocalDateTime;
import java.time.LocalTime;

import com.ice.studyroom.domain.room_timeslot.domain.type.DayOfWeekStatus;
import com.ice.studyroom.domain.room_timeslot.domain.type.RoomTimeSlotStatus;
import com.ice.studyroom.domain.room_timeslot.domain.type.RoomType;
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
@Table(name = "room_time_slot")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RoomTimeSlot extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "room_number", nullable = false)
	private String roomNumber;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private RoomType roomType;

	@Column(name = "capacity", nullable = false)
	private int capacity;

	//최소예약인원
	@Column(name = "min_res", nullable = false)
	private int minRes;

	@Column(name = "start_time", nullable = false)
	private LocalTime startTime;

	@Column(name = "end_time", nullable = false)
	private LocalTime endTime;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Builder.Default
	private RoomTimeSlotStatus status = RoomTimeSlotStatus.AVAILABLE;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private DayOfWeekStatus dayOfWeek;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Builder
	public RoomTimeSlot(String roomNumber, RoomType roomType, int capacity, int minRes, LocalTime startTime,
		LocalTime endTime, DayOfWeekStatus dayOfWeek) {
		this.roomNumber = roomNumber;
		this.roomType = roomType;
		this.capacity = capacity;
		this.minRes = minRes;
		this.startTime = startTime;
		this.endTime = endTime;
		this.dayOfWeek = dayOfWeek;
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
		validateTimeSlot();
	}

	/**
	 * 상태를 업데이트하는 메서드
	 *
	 * @param newStatus 업데이트할 상태
	 */
	public void updateStatus(RoomTimeSlotStatus newStatus) {
		if (this.status == RoomTimeSlotStatus.RESERVED && newStatus == RoomTimeSlotStatus.RESERVED) {
			throw new IllegalStateException("이미 선점된 상태입니다.");
		}
		this.status = newStatus;
	}

	private void validateTimeSlot() {
		if (startTime.isAfter(endTime)) {
			throw new IllegalArgumentException("시작 시간이 종료 시간보다 늦을 수 없습니다.");
		}

		if (startTime.isBefore(LocalTime.of(9, 0)) || endTime.isAfter(LocalTime.of(22, 0))) {
			throw new IllegalArgumentException("운영 시간(09:00-22:00) 내에서만 설정 가능합니다.");
		}
	}
}
