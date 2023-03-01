package com.cis.sys101_notifications.domain.enums;

import lombok.Getter;

@Getter
public enum Roles {

	CENTRAL_FIRE_STATION("Диспетчер ЦППС"),
	OPERATIONAL_SUPPORT_SERVICE("Диспетчер СОО (ДС)"),
	REGIONAL_GARRISON_COMMANDER("Начальник гарнизона (ТПСГ)"),
	LOCAL_GARRISON_COMMANDER("Начальник гарнизона (МПСГ)"),
	HEAD_OF_UNIT("Начальник части"),
	HEAD_OF_GUARD("Начальник караула"),
	FIRE_STATION("Диспетчер ПЧ"),
	AGENT("Руководитель СПТ и ПАСР");

	private final String description;

	Roles(String description) {
			this.description = description;
		}
}