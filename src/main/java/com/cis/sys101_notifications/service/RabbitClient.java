package com.cis.sys101_notifications.service;

import com.cis.sys101.util.RabbitCommand;
import com.cis.sys101.util.RabbitMessage;
import com.cis.sys101_notifications.config.AppConfig;
import com.cis.sys101_notifications.domain.ExternalDepartment;
import com.cis.sys101_notifications.domain.Notification;
import com.cis.sys101_notifications.domain.enums.EventPriority;
import com.cis.sys101_notifications.domain.enums.Roles;
import com.cis.sys101_notifications.dto.EventDto;
import com.cis.sys101_notifications.dto.GarrisonDto;
import com.cis.sys101_notifications.dto.Token;
import com.cis.sys101_notifications.repository.ExternalDepartmentRepository;
import com.cis.sys101_notifications.support.EventProcessor;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.rabbitmq.client.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

import  static  com.cis.sys101_notifications.config.SocketConfig.LoggedInUsers;

@Service
@EnableRabbit
@RequiredArgsConstructor
public class RabbitClient {

	@Value("${keycloak.resource}")
	private String resource;

	@Value("${keycloak.credentials.secret}")
	private String secret;

	@Value("${keycloak.realm}")
	private String realm;

	@Value("${spring.rabbitmq.host}")
	private String rabbitmqHost;

	@Value("${spring.rabbitmq.username}")
	private String rabbitmqUser;

	@Value("${spring.rabbitmq.password}")
	private String rabbitmqPassword;

	@Value("${spring.rabbitmq.port}")
	private int rabbitmqPort;


	private static  Logger log = LoggerFactory.getLogger(RabbitClient.class);

	private static Channel notificationChannel;
	private static Connection rmqConnection;
	private final AppConfig appConfig;
	private final EventProcessor eventProcessor;
	private final FeignService feignService;
	private final ExternalDepartmentRepository externalDepartmentRepository;


	public Channel initSyncChannel() {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(rabbitmqHost);
		factory.setUsername(rabbitmqUser);
		factory.setPassword(rabbitmqPassword);
		factory.setPort(rabbitmqPort);
		Channel channel = null ;
		try {
			rmqConnection = factory.newConnection();
			channel = rmqConnection.createChannel();
		} catch (Exception e) {
			log.error("Error  while connecting to RabbitMQ {} ",  e.getMessage());
		}
		return channel;
	}

	/**
	 * инициализация икченджа RabbitMQ, через очереди которого осуществляется рассылка оповещений
	 */
	public void initNotificationExchange() {
		try {
			notificationChannel = rmqConnection.createChannel();
			notificationChannel.exchangeDeclare(appConfig.getNotificationExchange(),  BuiltinExchangeType.DIRECT, true);
		} catch (IOException e) {
			log.error("Error  while declaring notifications' exchange  {} ",  e.getMessage());
		}
	}

	/**
	 * объявление очередей рассылки
	 * НО ЭТОТ ЖЕ МЕТОД И ЭТОТ ЖЕ КАНАЛ ИСПОЛЬЗУЕТСЯ
	 * ДЛЯ ОБЪЯВЛЕНИЯ ОЧЕРЕДЕЙ ЧТЕНИЯ ИЗ ИКСЧЕНДЖА ФОРСЕС ДЛЯ СИНХРОНИЗАЦИИ
	 * @param queueName  имя очереди
	 * @param exchange	иксчендж, за которым закреплена очередь
	 * @param bindKey	ключ закрепления очереди к иксченджу
	 */
	private void declareAndBindQueue(String queueName, String exchange, String  bindKey) {
		try {
			notificationChannel.queueDeclare(queueName, true, false, false, null);
			notificationChannel.queueBind(queueName, exchange, bindKey);
		} catch (IOException e) {
			log.error("Error while  declaring  or binding notification queues :{}", e.getMessage());
		}
	}

	/**
	 * базовый механизм публикации сообщений в очередь
	 * @param exchange  икчендж, куда будет опубликовано сообщение
	 * @param routingKey ключ адресации. не важен в случае FANOUT искченджа
	 * @param notification собственно публикуемое сообщение
	 */
	private  void publish(String exchange, String routingKey, String notification) {
		try {
			notificationChannel.basicPublish(exchange, routingKey, null, notification.getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			log.error("Error while  publishing notification:{}", e.getMessage());
		}
	}

	/**
	 * реализация слушателя иксченджа событий какого-либо из сервисов
	 * с рассылкой в ответ уведомлений  в иксчендж уведомлений
	 * @param queueName имя вновь создаваемой очереди, через которую реализуется чтение событий
	 * @param exchangeToReadFrom имя иксченджа-поставщика событий
	 * @param bindKey ключ привязки очереди к иксченджу
	 */
	public  void initEventListener(String queueName, String exchangeToReadFrom, String bindKey) {

		DeliverCallback deliverCallback = new DeliverCallback() {
			public void handle(String s, Delivery delivery) {

				Optional<String> eventMessageBody = Optional.empty();
				Envelope envelope = delivery.getEnvelope();

				String routingKey = envelope.getRoutingKey();	 // нужен ли

				try {
					eventMessageBody = Optional.ofNullable(new String(delivery.getBody(), "UTF-8"));
					log.info("RabbitMQ message: {}", eventMessageBody);
				} catch (UnsupportedEncodingException e) {
					log.error("Error while extracting RabbitMQ message: {}", e.getMessage());
				}

				if(eventMessageBody.isPresent()){
					try {
						eventProcessor
						.readEvent(eventMessageBody.get())
						.ifPresent(eventMessage -> {
							log.info("EventDto: {}", eventMessage);
							Notification notification = eventProcessor.writeNotificationToDb(eventMessage);
							log.info("Notification recorded to db: {}", notification);

							Map<EventPriority, String> notificationsToSend = eventProcessor.mapEventDtoToNotification(eventMessage, notification);
							appConfig.getRecipients().entrySet()
							.stream()
							.filter(entry -> entry.getKey().equals(eventMessage.getEventType()))
							.findFirst()
							.map(mapEntry -> mapEntry.getValue())
							.get()
							// recipient  -  grave.operational_support_service и т.д.; eventMessage - EventDto
							//.forEach(recipient -> publishNotificationsPerClient(eventMessage, recipient, notificationsToSend, appConfig.getNotificationExchange()));
							.forEach(recipient -> publishNotificationsPerClient(eventMessage, recipient, notificationsToSend, appConfig.getNotificationExchange()));
						}) ;
					} catch (Exception e) {
						log.error("Error while converting event message to notification: {}", e.getMessage());
					}
				}
			}
		};
		//  слушаем очереди событий
		try {
			log.info("Queue declared: queue - {}, exchange - {}", queueName, exchangeToReadFrom);
			notificationChannel.queueDeclare(queueName, true, false, false, null);
			log.info("1 Queue declared: queue {}", queueName);
			notificationChannel.queueBind(queueName, exchangeToReadFrom, bindKey);
			log.info("2 Queue binded: queue {}", queueName);
			// 2-nd arg - acknowledgement flag; 'true' removes the message from queue,  4-th arg - CancelCallback
			notificationChannel.basicConsume(queueName, true, deliverCallback, consumerTag -> {});
			log.info("3 Consumer started: queue {}", queueName);
		} catch (IOException e) {
			log.error("Error while reading event  message: {}", e.getMessage());
		}
	}

	/**
	 * формирование очередей ДЛЯ КАЖДОГО ПОЛЬЗОВАТЕЛЯ и публикация в них  уведомлений
	 * @param eventDto  DTO события
	 * @param recipient 	получатель уведомления - какой роли с каким приоритетом, напр., grave.operational_support_service
	 * @param notificationsToSend набор оповещений с ключом-приоритетом
	 * @param exchangeToSend иксчендж для публикации оповещений
	 */
	public void publishNotificationsPerClient(EventDto eventDto, String recipient, Map<EventPriority, String> notificationsToSend, String exchangeToSend) {

		List<String> fireStationIds = Arrays.asList(eventDto.getAttributes().getDepartmentInvolved());
		List<String> bindKeys = new ArrayList<>();
		String role = recipient.split("\\.")[1];

		if ("fire_station".equals(role) || "head_of_unit".equals(role) || "head_of_guard".equals(role) || "local_garrison_commander".equals(role) || "central_fire_station".equals(role)) {
			if (!ObjectUtils.isEmpty(fireStationIds)) {

				String roleAsEnum = Roles.valueOf(role.toUpperCase()).getDescription();
				log.info("roleAsEnum: {}", roleAsEnum);

				for (String fireStationId : fireStationIds) {
					/* проверка наличия пользователей с указанными ролями и подразделениями среди авторизовавшихся  */
					log.info("LoggedInUsers: {}", Arrays.asList(LoggedInUsers));
					LoggedInUsers.entrySet().stream().filter(user -> user.getValue().getRoles().contains(roleAsEnum) && user.getValue().getDepartmentId().equals(UUID.fromString(fireStationId)))
					.forEach(user -> {
						Optional<ExternalDepartment> extDep = externalDepartmentRepository.findById(UUID.fromString(fireStationId));
						extDep.ifPresent(vo -> {
							String key = user.getKey().toString();
							bindKeys.add(key);

							/* 	channel.queueDeclare seems to be idempotent if  durability, exclusivity, auto-deleted-ness are the same
									so we don't need to bother if the queue exists */
							declareAndBindQueue(key, exchangeToSend, key);
						});
					});
				}
			}
		} else {
			String roleAsEnum = Roles.valueOf(role.toUpperCase()).getDescription();
			LoggedInUsers.entrySet().stream().filter(user -> user.getValue().getRoles().contains(roleAsEnum))
			.forEach(user -> {
					String key = user.getKey().toString();
					bindKeys.add(key);
					declareAndBindQueue(key, exchangeToSend, key);
			});
		}
		for (String bindKey : bindKeys) {
			if (recipient.contains("standard"))
				publish(exchangeToSend, bindKey, notificationsToSend.get(EventPriority.STANDARD));
			else if (recipient.contains("grave"))
				publish(exchangeToSend, bindKey, notificationsToSend.get(EventPriority.GRAVE));
			else
				publish(exchangeToSend, bindKey, notificationsToSend.get(EventPriority.CRITICAL));
		}
	}

	/**
	 * формирование очередей и публикация в них  уведомлений для текущей роли [ПРЕДОПРЕДЕЛЕННЫЕ ОЧЕРЕДИ]
	 * @param eventDto  DTO события
	 * @param recipient получатель уведомления - какой роли с каким приоритетом, напр., grave.operational_support_service или critical.fire_station или ...
	 * @param notificationsToSend набор оповещений с ключом-приоритетом
	 * @param exchangeToSend иксчендж для публикации оповещений
	 */
	public void publishNotificationsPerRole(EventDto eventDto, String recipient, Map<EventPriority, String> notificationsToSend, String exchangeToSend) {

		List<String> fireStationIds = Arrays.asList(eventDto.getAttributes().getDepartmentInvolved());
		List<String> bindKeys = new ArrayList<>();
		String role = recipient.split("\\.")[1];

		if ("fire_station".equals(role) || "head_of_unit".equals(role) || "head_of_guard".equals(role)) {
			if (!ObjectUtils.isEmpty(fireStationIds)) {
				for (String fireStationId : fireStationIds) {
					Optional<ExternalDepartment> extDep = externalDepartmentRepository.findById(UUID.fromString(fireStationId));
					extDep.ifPresent(vo -> {
						String key = fireStationId + "." + role;
						bindKeys.add(key);
						declareAndBindQueue(key, exchangeToSend, key);
					});
				}
			}
		} else if ("local_garrison_commander".equals(role) || "central_fire_station".equals(role)) {
			if (!ObjectUtils.isEmpty(fireStationIds)) {
				for (String fireStation : fireStationIds) {
					Optional<GarrisonDto> localGarrisonDto = feignService.getGarrisonByDepartmentId(getToken(), UUID.fromString(fireStation));
					localGarrisonDto.ifPresent(garrisonDto -> {
						String garrisonKey = garrisonDto.getGarrisonId() + "/" + role;
						bindKeys.add(garrisonKey);
						declareAndBindQueue(garrisonKey, exchangeToSend, garrisonKey);
					});
				}
			}
		}  else {
			bindKeys.add(role);
			declareAndBindQueue(role, exchangeToSend, role);
		}

		for (String bindKey : bindKeys) {
			if (recipient.contains("standard"))
				publish(exchangeToSend, bindKey, notificationsToSend.get(EventPriority.STANDARD));
			else if (recipient.contains("grave"))
				publish(exchangeToSend, bindKey, notificationsToSend.get(EventPriority.GRAVE));
			else
				publish(exchangeToSend, bindKey, notificationsToSend.get(EventPriority.CRITICAL));
		}
	}


	private String getToken() {

		Token token = null;
		HttpClient client = HttpClients.createDefault();
		String auth = resource + ":" + secret;
		String basic = Base64.encodeBase64String(auth.getBytes());

		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost("172.16.0.157").setPort(8180)
		.setPath("/auth/realms/" + realm + "/protocol/openid-connect/token");

		try {
			URI postURI = builder.build();
			HttpPost post = new HttpPost(postURI);
			post.addHeader("Authorization", "Basic " + basic);
			post.addHeader("content-type", "application/x-www-form-urlencoded");
			//post.setEntity(new StringEntity("username=admin&password=admin&grant_type=password"));
			post.setEntity(new StringEntity("username=bombero1&password=bombero1&grant_type=password"));

			CloseableHttpResponse response = (CloseableHttpResponse) client.execute(post);

			HttpEntity entity = response.getEntity();
			String json1 = EntityUtils.toString(entity, StandardCharsets.UTF_8);
			Gson gson = new Gson();
			token = gson.fromJson(json1, Token.class);
		} catch (IOException | ParseException | JsonSyntaxException | URISyntaxException e) {
			log.error("Error while  parsing token:{}", e.getMessage());
		}

		return "Bearer " + token.getAccess_token();
	}


	public void initSyncListener(Channel channel, String queueName, String exchange, String bindKey, Consumer<RabbitMessage> consumer) {

		DeliverCallback deliverCallback = new DeliverCallback() {
			public void handle(String s, Delivery delivery) {

				RabbitMessage rabbitMessage = null;
				try {
					rabbitMessage = (RabbitMessage) SerializationUtils.deserialize(delivery.getBody());
					log.info("RabbitMQ message: {}", rabbitMessage);
				} catch (Exception e) {
					log.error("Error while extracting RabbitMQ message: {}", e.getMessage());
				}

				if (rabbitMessage == null || rabbitMessage.getCommand() == null)
					rabbitMessage = RabbitMessage.of(RabbitCommand.UPDATE_ALL, null);

				consumer.accept(rabbitMessage);
			}
		};
		try {
			declareAndBindQueue(queueName, exchange, bindKey);
			channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {	});

		} catch (IOException e) {
			log.error("Error while consuming message: " + e.getMessage());
		}
	}
}