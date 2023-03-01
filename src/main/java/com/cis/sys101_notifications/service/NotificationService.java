package com.cis.sys101_notifications.service;

import com.cis.sys101_notifications.domain.Notification;

import java.util.UUID;

public interface NotificationService {

	/**
	 * Обновляет  статус отработанного уведомления до confirmed
	 * @param id 	идентификатор отработанного уведомления
	 * @param userId 	идентификатор пользователя, работавшего с критическим  уведомлением
	 * @param result 	ответ на  критическое  уведомление
	 */
	void confirmNotificationProcessed(UUID id, UUID userId, String result) ;

	/**
	 * Регистрирует  уведомление в базе  как реакция на поступившее извне  событие
	 * @param notification	фиксируемое  уведомление
	 * @return Notification сформированное уведомление
	 */
	Notification upsertNotification(Notification notification);
}