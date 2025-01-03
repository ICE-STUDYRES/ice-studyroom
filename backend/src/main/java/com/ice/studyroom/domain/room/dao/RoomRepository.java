package com.ice.studyroom.domain.room.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ice.studyroom.domain.room.domain.entity.Room;

public interface RoomRepository extends JpaRepository<Room, Long> {
}
