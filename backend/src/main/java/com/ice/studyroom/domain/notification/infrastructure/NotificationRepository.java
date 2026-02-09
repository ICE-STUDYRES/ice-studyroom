package com.ice.studyroom.domain.notification.infrastructure;

import com.ice.studyroom.domain.notification.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

	// 안 읽은 알림만 가져오기
	@Query("""
        select n
        from Notification n
        where n.memberId = :memberId
          and n.isRead = false
        order by n.createdAt desc
    """)
	List<Notification> findUnreadByMemberId(Long memberId);
}

