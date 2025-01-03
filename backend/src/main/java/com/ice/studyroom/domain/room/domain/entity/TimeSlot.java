package com.ice.studyroom.domain.room.domain.entity;

import java.time.LocalDateTime;
import java.time.LocalTime;

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

@Entity
@Table(name = "time_slot")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TimeSlot extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "start_time", nullable = false)
	private LocalTime startTime;

	@Column(name = "end_time", nullable = false)
	private LocalTime endTime;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Builder
	public TimeSlot(LocalTime startTime, LocalTime endTime) {
		this.startTime = startTime;
		this.endTime = endTime;
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
		validateTimeSlot();
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
