package com.cis.sys101_notifications.repository;

import com.cis.sys101_notifications.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

// no need for @Repository if @EnableJpaRepositories is present
public interface NotificationRepository  extends JpaRepository<Notification, Long> {

	@Modifying
	@Query("update Notification n set n.isProcessed = true, n.processedAt = current_timestamp, n.processedBy = :userId, n.result = :result where id = :notificationId")
	void confirmNotificationProcessed(@Param("notificationId") UUID notificationId, @Param("userId") UUID userId, @Param("result") String result);
}