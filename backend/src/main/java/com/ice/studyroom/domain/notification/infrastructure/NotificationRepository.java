package com.ice.studyroom.domain.notification.infrastructure;

import com.ice.studyroom.domain.notification.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

	// 안 읽은 알림만 가져오기
	List<Notification> findByMemberIdAndIsReadFalseOrderByCreatedAtDesc(Long memberId);
}
