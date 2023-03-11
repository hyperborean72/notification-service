# notification-service
Notification service implemented for [XYZ] in 2022. Accepts other services' event messages from Rabbit MQ to produce notifications for the clients authenticated with Keycloak and connected via websocket. Part of a greater microservice system based on Spring Cloud Netflix and Feign


[Notifications manual June'22.pdf](https://github.com/hyperborean72/notification-service/files/10947416/Notifications.manual.June.22.pdf)

Сервис уведомлений представляет собой middleware (промежуточное ПО):
- слушающее сообщения об определенных событиях из набора очередей сервера RabbitMQ
- фиксирующее в БД факт наступления события
- формирующее в ответ на сообщение о событии уведомление (либо ряд уведомлений согласно приоритетам)
- реализующее рассылку уведомлений получателям согласно их роли
- фиксирующее в БД результат обработки уведомления с приоритетом "Критическое" в ответ на отправленное клиентом через сокет подтверждающее сообщение 

***********************************************************************************************

Внешней по отношению к событию, то есть передаваемой в составе конфигурационного файла сервиса уведомлений, является информация о:
- шаблонах уведомлений
- получателях уведомлений соответственно типу события и приоритету уведомления
- иксчендж-объекте и очередях событий
- иксчендж-объекте очереди уведомлений
  
События читаются согласно следующим настройкам из конфигурационного файла:
![image](https://user-images.githubusercontent.com/9664260/224461045-b88762c2-f901-4407-9132-38f2b167659c.png)

Здесь 	incidentsExchange (после двоеточия)- иксчендж RabbitMQ, в который сервис инцидентов публикует события;
	incidents (до двоеточия) - очередь, которая будет автоматически сформирована сервисом для чтения событий, публикуемых сервисом инцидентов

Другие сервисы также станут поставщиками событий - каждому будет назначен собственный икчендж событий

***********************************************************************************************

События из сервиса инцидентов:
- CALL_REGISTERED			Зарегистрирован новый вызов,
- COMBAT_DEPARTURE_REGISTERED	Зарегистрирован новый боевой выезд,
- NON_COMBAT_DEPARTURE_REGISTERED	Зарегистрирован новый небоевой выезд,
- STUDY_DEPARTURE_REGISTERED	Зарегистрирован новый учебный выезд,
- REQUEST_FOR_FORCES		Запрос на высылку дополнительных сил и средств,
- REQUEST_FOR_FORCES_DECLINED	Запрос на высылку сил и средств отклонён,
- REQUEST_FOR_FORCES_CONFIRMED	Запрос на высылку сил и средств подтверждён,
- DEATH_TOLL_CHANGE		Изменение числа погибших,
- FIRE_RANK_CHANGE			Изменение ранга пожара,
- COMBAT_DEPARTURE_STATUS_CHANGE	Изменение статуса боевого выезда,
- LACK_OF_REPORT			Отсутствие актуального доклада,
- GARRISONS_TOOK_OVER		Все дежурные караулы гарнизона заступили,
- STATUS_GUARD_ON_DUTY		Статус дежурного караула подразделения - Заступил,
- GUARD_ON_DUTY_CHANGE		Изменение состава дежурного караула;
- GUARD_ON_DUTY_STATUS_CONFIRMED	Статус дежурного караула подразделения – «Подтверждено ЦППС»

![схема рассылки уведомлений](https://user-images.githubusercontent.com/9664260/224461157-85fa1af9-60bd-4d92-91c8-d40df988cf10.jpg)

События из сервиса пожароопасных объектов:
- OBJECT_STATUS_CHANGE		Изменен  статус пожароопасноого объекта


Пример события, в ответ на которое будет сформирован ряд уведомлений с соответствующим приоритетом
```{
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

здесь 	
- senderName  - имя сервиса-отправителя
- unitShortName - краткое наименование ПЧ - источника уведомления
- eventId - UUID сообщения о событии


В секции attributes перечислены (необязательные) параметры заполнения шаблона уведомления.


Дальнейшая информация о частях и гарнизонах, где уведомление должно быть получено, включая наименование и принадлежность гарнизонам, извлекается в сервисе по uuid подразделений, перечисленных в секции department_involved

С шаблонами уведомлений для каждого события и приоритета можно ознакомиться в документе "Уведомления - табличка.doc"

Порядковый номер (uuid) уведомления "зашит" в уведомление и представляет собой его первые 36 символов.
За uuid уведомления следует точка и собственно текст уведомления.

***********************************************************************************************
Клиент должен быть подключен к конечной точке (STOMP endpoint) по адресу /notificationWebsocket:
	var socket = new SockJS('/notificationWebsocket');
	stompClient = Stomp.over(socket);


Получатели уведомлений идентифицируются сервисом согласно следующей схеме:
Диспетчер ПЧ	fire_station	Диспетчер ПЧ
Начальник караула	head_of_guard	Начальник караула
Начальник части	head_of_unit	Начальник части
		
Начальник МПСГ	local_garrison_commander	Начальник гарнизона (МПСГ)
		
Диспетчер ЦППС	central_fire_station	Диспетчер ЦППС
Диспетчер СООДС	operational_support_service	Диспетчер СОО(ДС)
Исполнитель СПТ	agent	Исполнитель СПТ и ПАСР
Начальник ТПСГ	regional_garrison_commander	Начальник гарнизона  (ТПСГ)

Чтение уведомлений реализуется подключением к точкам доступа:
Диспетчер ПЧ	/queue/{uuid части}.fire_station
Начальник караула	/queue/{uuid части}.head_of_guard
Начальник части	/queue/{uuid части}.head_of_unit
	
Начальник МПСГ	/queue/{uuid гарнизона}.local_garrison_commander
Диспетчер ЦППС	/queue/{uuid гарнизона}.central_fire_station
	
Диспетчер СООДС	/queue/operational_support_service
Исполнитель СПТ	/queue/agent
Начальник ТПСГ	/queue/regional_garrison_commander

Примеры подключения для чтения уведомлений:
stompClient.subscribe('/queue/operational_support_service', onMessageReceived);
stompClient.subscribe('/queue/274b7f42-e400-4cd0-992f-3d68c059bf2f.fire_station', onMessageReceived)


Подтверждение обработки критического уведомления реализуется подключением к точке /app/notificationProcessed, 
как в примере из файла socket.js:
	stompClient.send("/app/notificationProcessed", {}, JSON.stringify({
		id : ''9ee5a41e-21b1-4e29-96d3-db0d9b98bc99",
		userId: 'ae80d1e8-b348-436e-ad13-058c49e2bfe6',
        	result: 'Исполнено'
	}))
	
Ответ на критическое уведомление должен содержать следующие поля:
	id 	- порядковый номер (uuid) уведомления
	userId 	- порядковый номер (uuid) пользователя системы, принявшего уведомление
	result	- действие, предпринятое в ответ на уведомление


Порядковый номер (uuid) уведомления "зашит" в уведомление и представляет собой его первые 36 символов.
За uuid уведомления следует точка и собственно текст уведомления.

