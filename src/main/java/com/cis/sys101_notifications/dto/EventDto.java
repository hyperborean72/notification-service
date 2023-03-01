package com.cis.sys101_notifications.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties
@Data
// Набор атрибутов события, из которых формируется уведомление
public class EventDto {
	private String senderName;									// сервер - источник события
	private String eventType;										// тип события
	private String  eventDate;										// дата регистрации (формирования, заступления) в зависимости от типа события
	private TemplateAttributes attributes;

	@NoArgsConstructor
	@Data
	public class TemplateAttributes {
		private String regNumber;											// регистрационный номер выезда, optional
		private String address;													// адрес выезда, optional
		private String date;
		private String departureStatus;								// новый статус боевого выезда, событие COMBAT_DEPARTURE_STATUS_CHANGE, optional
		private String fireRank;													// новый ранг, optional
		private String taskType;												// тип задачи для небоевых выездов NON_COMBAT_DEPARTURE_REGISTERED и STUDY_DEPARTURE_REGISTERED, optional
		private String employee;												// ответственный за выезд сотрудник, optional
		private int deathRate;													// количество погибших по каждому виду, optional
		private String garrisonShortName;						// краткое наименование гарнизона: GUARD_ON_DUTY_STATUS_CONFIRMED, GUARD_ON_DUTY_CHANGE, GARRISONS_TOOK_OVER
		private String unitShortName;									// наименование подразделения регистрации, optional
		private String[]  departmentInvolved;					// перечень uuid подразделений-адресатов уведомления
		private String objectName;										// пожароопасный объект, статус которого был изменен
		private String objectStatus;										// значение статуса пожароопасного объекта после изменения
		private String incidentId;												// uuid происшествия, для формирования ссылки на высылку допСиС, optional
		private String responsibleDepartmentName;	// наименование ответственного подразделения, чей район выезда, optional
		private String responsibleDepartmentId; 			// id ответственного подразделения, optional
		private String regNumberCall;									// регистрационный номер вызова, optional
		private String callDepartmentName;					//  наименование подразделения, зарегистрировавшего вызов
	}
}