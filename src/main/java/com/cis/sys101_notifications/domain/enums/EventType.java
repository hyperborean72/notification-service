package com.cis.sys101_notifications.domain.enums;

public enum EventType {
	COMBAT_DEPARTURE_REGISTERED("Зарегистрирован новый боевой выезд"),
	NON_COMBAT_DEPARTURE_REGISTERED("Зарегистрирован новый небоевой выезд"),
	STUDY_DEPARTURE_REGISTERED("Зарегистрирован новый учебный выезд"),
	REQUEST_FOR_FORCES("Запрос на высылку дополнительных сил и средств"),
	REQUEST_FOR_FORCES_DECLINED("Запрос на высылку сил и средств отклонён"),
	REQUEST_FOR_FORCES_CONFIRMED("Запрос на высылку сил и средств подтверждён"),
	DEATH_TOLL_CHANGE("Изменение числа погибших"),
	FIRE_RANK_CHANGE("Изменение ранга пожара"),
	COMBAT_DEPARTURE_STATUS_CHANGE("Изменение статуса боевого выезда"),
	LACK_OF_REPORT("Отсутствие актуального доклада"),
	GARRISONS_TOOK_OVER("Все дежурные караулы гарнизона заступили"),
	STATUS_GUARD_ON_DUTY("Статус дежурного караула подразделения - Заступил"),
	GUARD_ON_DUTY_CHANGE("Изменение состава дежурного караула"),
	GUARD_ON_DUTY_STATUS_CONFIRMED ("Статус дежурного караула подразделения - Подтверждено ЦППС "),
	OBJECT_STATUS_CHANGE("Изменен  статус пожароопасноого объекта"),
	CALL_REGISTERED ("Зарегистрирован новый вызов ");

	private final String description;

	EventType(String description) {
		this.description = description;
	}
}