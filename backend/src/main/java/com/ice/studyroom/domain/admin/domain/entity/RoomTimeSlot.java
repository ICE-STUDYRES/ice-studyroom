package com.ice.studyroom.domain.admin.domain.entity;

import java.time.LocalTime;

import com.ice.studyroom.domain.admin.domain.type.DayOfWeekStatus;
import com.ice.studyroom.domain.admin.domain.type.RoomType;
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
@Table(name = "room_time_slot")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RoomTimeSlot extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "room_number", nullable = false, length = 20)
	private String roomNumber;

	@Enumerated(EnumType.STRING)
	@Column(name = "room_type", nullable = false)
	@Builder.Default
	private RoomType roomType = RoomType.GROUP;

	@Column(name = "capacity", nullable = false)
	@Builder.Default
	private int capacity = 4;

	//최소예약인원
	@Column(name = "min_res", nullable = false)
	@Builder.Default
	private int minRes = 2;

	@Column(name = "start_time", nullable = false)
	private LocalTime startTime;

	@Column(name = "end_time", nullable = false)
	private LocalTime endTime;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Builder.Default
	private ScheduleSlotStatus status = ScheduleSlotStatus.AVAILABLE;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private DayOfWeekStatus dayOfWeek;

	public void updateStatus(ScheduleSlotStatus newStatus) {
		this.status = newStatus;
	}

}
