package com.cis.sys101_notifications.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class DepartmentDto {

	private UUID id;
	private String shortName;
	private String name;
}