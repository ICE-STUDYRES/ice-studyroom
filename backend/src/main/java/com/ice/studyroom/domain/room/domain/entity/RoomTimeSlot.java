package com.ice.studyroom.domain.room.domain.entity;

import com.ice.studyroom.global.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "room_time_slot")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoomTimeSlot extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "room_number", nullable = false, unique = true)
	private String roomNumber;

	@Column(name = "capacity", nullable = false)
	private int capacity;

	@Column(name = "is_active", nullable = false)
	private boolean isActive;

	@Column(name = "start_time", nullable = false)
	private LocalTime startTime;

	@Column(name = "end_time", nullable = false)
	private LocalTime endTime;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;


	@Builder
	public RoomTimeSlot(String roomNumber, int capacity, LocalTime startTime, LocalTime endTime) {
		this.roomNumber = roomNumber;
		this.capacity = capacity;
		this.isActive = true;
		this.startTime = startTime;
		this.endTime = endTime;
		validateTimeSlot();
	}

	// TimeSlot 검증 메서드
	private void validateTimeSlot() {
		if (startTime.isAfter(endTime)) {
			throw new IllegalArgumentException("시작 시간이 종료 시간보다 늦을 수 없습니다.");
		}
		if (startTime.isBefore(LocalTime.of(9, 0)) || endTime.isAfter(LocalTime.of(22, 0))) {
			throw new IllegalArgumentException("운영 시간(09:00-22:00) 내에서만 설정 가능합니다.");
		}
	}

	// TimeSlot 업데이트 메서드
	public void updateTimeSlot(LocalTime startTime, LocalTime endTime) {
		this.startTime = startTime;
		this.endTime = endTime;
		validateTimeSlot();
	}
}
