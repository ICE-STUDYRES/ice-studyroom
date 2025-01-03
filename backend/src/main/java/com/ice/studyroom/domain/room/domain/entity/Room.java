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

@Entity
@Table(name = "room")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Room extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "room_number", nullable = false, unique = true)
	private String roomNumber;

	@Column(name = "capacity", nullable = false)
	private int capacity;

	@Column(name = "is_active", nullable = false)
	private boolean isActive;

	@Builder
	public Room(String roomNumber, int capacity) {
		this.roomNumber = roomNumber;
		this.capacity = capacity;
		this.isActive = true;
	}
}
