# notification-service
Notification service implemented for [XYZ] in 2022. Accepts other services' event messages sent via  Rabbit MQ to produce notifications for the clients authenticated with Keycloak and connected via websocket. Part of a greater microservice system based on Spring Cloud Netflix and Feign


[Notifications manual June'22.pdf](https://github.com/hyperborean72/notification-service/files/10947416/Notifications.manual.June.22.pdf)

The notification service is a middleware:
- listening for event messages from the RabbitMQ queues
 - writing to the database on the event occurrence 
  - generating notifications in response to event messages (or a series of notifications according to priorities)
   - sending notifications to recipients according to their role
   - registering  result of the critical notification processing into the database
***********************************************************************************************

The following information is external to the event, i.e. transmitted as part of the service configuration file:
- notification templates
- notification recipients according to event type and notification priority
- event exchange and queues
- notification exchange

  
Events are read according to the following settings from the configuration file:

![image](https://user-images.githubusercontent.com/9664260/224461045-b88762c2-f901-4407-9132-38f2b167659c.png)

Here, incidentsExchange is the RabbitMQ exchange to which the incident service publishes event messages and used as the source of events 

Other services are event providers as well - each operates via its own event exchange

***********************************************************************************************

Events from the incidents service:
- CALL_REGISTERED
- COMBAT_DEPARTURE_REGISTERED
- NON_COMBAT_DEPARTURE_REGISTERED
- STUDY_DEPARTURE_REGISTERED
- REQUEST_FOR_FORCES
- REQUEST_FOR_FORCES_DECLINED
- REQUEST_FOR_FORCES_CONFIRMED
- DEATH_TOLL_CHANGE
- FIRE_RANK_CHANGE
- COMBAT_DEPARTURE_STATUS_CHANGE
- LACK_OF_REPORT
- GARRISONS_TOOK_OVER
- STATUS_GUARD_ON_DUTY
- GUARD_ON_DUTY_CHANGE
- GUARD_ON_DUTY_STATUS_CONFIRMED

![схема рассылки уведомлений](https://user-images.githubusercontent.com/9664260/224461157-85fa1af9-60bd-4d92-91c8-d40df988cf10.jpg)

Events from the service of the fire hazardous objects:
- OBJECT_STATUS_CHANGE


Example of the incident message:
```json
{
	"senderName": "incidents", 
	"unitShortName": "ПЧ-108",
	"eventType": "COMBAT_DEPARTURE_REGISTERED",
	"eventDate": "Mon-01-11-2021 16:30:25",
	"eventId": "1b4e2e6e-7f4e-47a7-9604-eb716bb28838",	
	"attributes": {
		"reg_number": "абв-123",
		"address": "проспект Алибабы и сорока разбойников, 4",
		"date": "Tue-02-11-2021 08:30:25",		
		"departure_status": "БД на месте пожара",
		"department_involved": [ "274b7f42-e400-4cd0-992f-3d68c059bf2f ", "f18bba32-b98b-4c40-bdae-35cfc00bd24d" ]
	}	 
}
```
The authenticated user  sends STOMP authentication message via websocket. 
The service extracts user  id and other claims from the message headers, and composes notification for that user according to its role in the process.
Then it declares a new queue specific for that use, composes notification(s)  and sends  to the queue for processing.   
If the priority of the notification is critical, the user acknowledges its processing. 


***********************************************************************************************
The client must be connected to the STOMP endpoint /notificationWebsocket:
```'
	var socket = new SockJS('/notificationWebsocket');
	stompClient = Stomp.over(socket);
````
Example of the notifications reading:
````
stompClient.subscribe('/queue/operational_support_service', onMessageReceived);
stompClient.subscribe('/queue/274b7f42-e400-4cd0-992f-3d68c059bf2f.fire_station', onMessageReceived)
````

Acknowledgment of the critical notification processing sent to the /app/notificationProcessed point:
```
	stompClient.send("/app/notificationProcessed", {}, JSON.stringify({
		id : ''9ee5a41e-21b1-4e29-96d3-db0d9b98bc99",
		userId: 'ae80d1e8-b348-436e-ad13-058c49e2bfe6',
        	result: 'Done'
	}))
```	
