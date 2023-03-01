package com.cis.sys101_notifications.dto;

import lombok.Getter;
import lombok.Setter;

// Набор атрибутов, передаваемых клиентом на сервер при исполнении уведомления
@Getter
@Setter
public class NotificationDto {
	private String id;							//id уведомления
//	private String type;						//тип события
//	private String content;				//текст уведомления
//	private String createdDate;
	private String userId;					//uuid пользователя, обработавшего уведомление
	private String result;					//результат обработки уведомления
}