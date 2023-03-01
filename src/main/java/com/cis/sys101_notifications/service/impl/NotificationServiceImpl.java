package com.cis.sys101_notifications.service.impl;

import com.cis.sys101_notifications.domain.Notification;
import com.cis.sys101_notifications.repository.NotificationRepository;
import com.cis.sys101_notifications.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class NotificationServiceImpl  implements NotificationService {

	private NotificationRepository repository;

	public  NotificationServiceImpl(NotificationRepository r){
		repository = r;
	}

	@Override
	public void confirmNotificationProcessed(UUID id, UUID userId, String result) {
		repository.confirmNotificationProcessed(id, userId, result);
	}

	@Override
	public Notification upsertNotification(Notification notification) {
		return repository.saveAndFlush(notification);
	}
}