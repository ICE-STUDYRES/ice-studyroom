package com.ice.studyroom.domain.notification.infrastructure;

import com.ice.studyroom.domain.notification.domain.entity.Notification;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

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

	// 단건 알람 읽음처리를 위한 알림 하나 가져오기
	@Query("""
		select n
		from Notification n
		where n.id = :notificationId
		  and n.memberId = :memberId
	""")
	Optional<Notification> findForRead(
		@Param("notificationId") Long notificationId,
		@Param("memberId") Long memberId
	);

	// 전체 알람 읽음 처리를 위한 쿼리
	@Modifying(clearAutomatically = true)
	@Query("""
        update Notification n
        set n.isRead = true
        where n.memberId = :memberId
          and n.isRead = false
    """)
	int markAllAsRead(@Param("memberId") Long memberId);

}

