package com.cis.sys101_notifications.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class GarrisonDto {

	private UUID garrisonId;	//  в данном случае это id именно гарнизона, а не его родительского гарнизона

	private String garrisonNameShort;

	private String garrisonNameFull;

	private UUID headId;

	private String callingDepartmentShortName;
}