package com.cis.sys101_notifications.support;

import com.cis.sys101_notifications.config.AppConfig;
import com.cis.sys101_notifications.domain.Notification;
import com.cis.sys101_notifications.domain.enums.EventPriority;
import com.cis.sys101_notifications.dto.EventDto;
import com.cis.sys101_notifications.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class EventProcessor {

	private final ModelMapper modelMapper;
	private final  NotificationService notificationService;
	private final  AppConfig appConfig;


	/**
	 * Формирование объекта события из сообщения сервиса-источника
	 * @param eventMessageBody  сообщение из внешнего сервиса
	 * @return сформированное событие
	 * @throws Exception возможно в случае сообщения не в формате JSON
	 */
	public  Optional<EventDto> readEvent(String eventMessageBody) throws  Exception{

		ObjectMapper mapper = new ObjectMapper();
		 return Optional.ofNullable(mapper.readValue(eventMessageBody, EventDto.class));
	}


	/**
	 * Основной процессор событий, генерирующий набор оповещений в ответ на событие
	 * @param event  DTO события
	 * @param notification  Уже записанное в базу при чтении события оповещение, в котором нет текста, зато есть ID, который позволит идентифицировать оповещение на стороне клиента
	 * @return набор оповещений для данного события с ключом-приоритетом
	 */
	public Map<EventPriority, String> mapEventDtoToNotification(EventDto event, Notification notification){

		Map<EventPriority, String> result = new HashMap<>();
		String eventType = event.getEventType();
		List<String> templates = appConfig.getTemplates().get(eventType);
		// JDK 8 @server  does not support combined case (2,3; 4,10) and does not support expressions like EventType.COMBAT_DEPARTURE_STATUS_CHANGE.name()
		switch (eventType) {
			case "COMBAT_DEPARTURE_REGISTERED":
					result.put(EventPriority.CRITICAL,
					MessageFormat.format(templates.get(0), event.getAttributes().getRegNumber(), event.getAttributes().getAddress(), formatEventDate(event), event.getAttributes().getResponsibleDepartmentName(), EventPriority.CRITICAL.toString()));
					result.put(EventPriority.GRAVE,
					MessageFormat.format(templates.get(0), event.getAttributes().getRegNumber(), event.getAttributes().getAddress(), formatEventDate(event), event.getAttributes().getResponsibleDepartmentName(), EventPriority.GRAVE.toString()));
				break;
			case "NON_COMBAT_DEPARTURE_REGISTERED":
				result.put(EventPriority.STANDARD,
				MessageFormat.format(templates.get(0), event.getAttributes().getRegNumber(), formatEventDate(event), event.getAttributes().getTaskType(), event.getAttributes().getEmployee(), EventPriority.STANDARD.toString()));
				break;
			case "STUDY_DEPARTURE_REGISTERED":
				result.put(EventPriority.STANDARD,
				MessageFormat.format(templates.get(0), event.getAttributes().getRegNumber(), formatEventDate(event), event.getAttributes().getTaskType(), event.getAttributes().getEmployee(), EventPriority.STANDARD.toString()));
				break;
			case "LACK_OF_REPORT":
				result.put(EventPriority.CRITICAL, MessageFormat.format(templates.get(0), event.getAttributes().getRegNumber(), event.getAttributes().getAddress(), EventPriority.CRITICAL.toString()));
				break;
			case"REQUEST_FOR_FORCES":
				result.put(EventPriority.CRITICAL, MessageFormat.format(templates.get(0), event.getAttributes().getRegNumber(), event.getAttributes().getAddress(), EventPriority.CRITICAL.toString()));
				break;
			case "REQUEST_FOR_FORCES_DECLINED":
				result.put(EventPriority.CRITICAL, MessageFormat.format(templates.get(0),  event.getAttributes().getUnitShortName(), event.getAttributes().getRegNumber(), event.getAttributes().getAddress(), EventPriority.CRITICAL.toString()));
				break;
			case "REQUEST_FOR_FORCES_CONFIRMED":
				result.put(EventPriority.STANDARD, MessageFormat.format(templates.get(0),  event.getAttributes().getUnitShortName(), formatEventDate(event), event.getAttributes().getRegNumber(), event.getAttributes().getAddress(), EventPriority.STANDARD.toString()));
				break;
			case "DEATH_TOLL_CHANGE":
				result.put(EventPriority.STANDARD, MessageFormat.format(templates.get(0), event.getAttributes().getDeathRate(),  event.getAttributes().getRegNumber(), event.getAttributes().getAddress(), event.getAttributes().getUnitShortName(), EventPriority.STANDARD.toString()));
				break;
			case "FIRE_RANK_CHANGE":
				result.put(EventPriority.STANDARD, MessageFormat.format(templates.get(0), event.getAttributes().getFireRank(), event.getAttributes().getRegNumber(), event.getAttributes().getAddress(), formatEventDate(event), event.getAttributes().getUnitShortName(), EventPriority.STANDARD.toString()));
				break;
			case "COMBAT_DEPARTURE_STATUS_CHANGE":
				result.put(EventPriority.STANDARD, MessageFormat.format(templates.get(0), event.getAttributes().getRegNumber(), event.getAttributes().getAddress(), formatEventDate(event), event.getAttributes().getUnitShortName(), event.getAttributes().getDepartureStatus(), EventPriority.STANDARD.toString()));
				break;
			case "GUARD_ON_DUTY_STATUS_CONFIRMED":
				result.put(EventPriority.STANDARD, MessageFormat.format(templates.get(0), event.getAttributes().getUnitShortName(), event.getAttributes().getGarrisonShortName(), event.getAttributes().getEmployee(), EventPriority.STANDARD.toString()));
				break;
			case "GUARD_ON_DUTY_CHANGE":
					result.put(EventPriority.CRITICAL, MessageFormat.format(templates.get(0), event.getAttributes().getUnitShortName(), formatEventDate(event), EventPriority.CRITICAL.toString()));
					result.put(EventPriority.GRAVE, MessageFormat.format(templates.get(1), event.getAttributes().getUnitShortName(), event.getAttributes().getGarrisonShortName(), EventPriority.GRAVE.toString()));
					result.put(EventPriority.STANDARD, MessageFormat.format(templates.get(2), EventPriority.STANDARD.toString()));
				break;
			case "STATUS_GUARD_ON_DUTY":
				result.put(EventPriority.GRAVE, MessageFormat.format(templates.get(0), event.getAttributes().getUnitShortName(), formatEventDate(event), EventPriority.GRAVE.toString()));
				break;
			case "GARRISONS_TOOK_OVER":
				result.put(EventPriority.GRAVE, MessageFormat.format(templates.get(0), event.getAttributes().getGarrisonShortName(), formatEventDate(event), EventPriority.GRAVE.toString()));
				break;
			case "OBJECT_STATUS_CHANGE":
				result.put(EventPriority.STANDARD, MessageFormat.format(templates.get(0), event.getAttributes().getObjectName(), event.getAttributes().getObjectStatus(), EventPriority.STANDARD.toString()));
				break;
			case "CALL_REGISTERED":
				result.put(EventPriority.CRITICAL,
				MessageFormat.format(templates.get(0), event.getAttributes().getRegNumberCall(), event.getAttributes().getAddress(), formatEventDate(event), event.getAttributes().getCallDepartmentName(), EventPriority.CRITICAL.toString()));
				break;
		}
		return result;
	}

	private  String formatEventDate(EventDto eventDto){
		DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(new Locale("ru"));
		return formatter.format(LocalDateTime.parse(eventDto.getEventDate()));
	}

	/**
	 * регистрация события в базе
	 * @param event  DTO события
	 * @return	оповещение, зарегистрированное в базе  в ответ на событие
	 */
	public Notification writeNotificationToDb(EventDto event){
		Notification notification = modelMapper.map(event, Notification.class);
		log.info("Notification ready to upsert: {}", notification);
		return notificationService.upsertNotification(notification);
	}
}