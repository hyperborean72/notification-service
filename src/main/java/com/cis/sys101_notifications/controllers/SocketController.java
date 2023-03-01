package com.cis.sys101_notifications.controllers;

import com.cis.sys101_notifications.dto.NotificationDto;
import com.cis.sys101_notifications.service.NotificationService;
import com.cis.sys101_notifications.service.impl.NotificationServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Api(tags = "socket controller")
@Controller
public class SocketController {

	private NotificationService notificationService;

	public  SocketController (NotificationServiceImpl service){
		notificationService = service;
	}

	/* NOTE:
	 * destinations of the in-bound (in controller) and the out-bound (out of controller)  messages are the same
	 * but the prefix '/app' for in-bound message like '/app/notificationProcessed' is replaced with '/topic' prefix
	 * being sent from controller to message broker like ''/topic/notificationProcessed'
	 * */
	@ApiOperation(value = "Метод, фиксирующий в базе отработку критического  уведомления ")
	@MessageMapping("/notificationProcessed")
	//@SendTo("/topic/notificationProcessed")
	public void notificationProcessed(@Payload NotificationDto notificationDto) {
		notificationService.confirmNotificationProcessed(UUID. fromString(notificationDto.getId()), UUID. fromString(notificationDto.getUserId()), notificationDto.getResult());
	}
}