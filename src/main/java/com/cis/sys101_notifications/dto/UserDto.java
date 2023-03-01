package com.cis.sys101_notifications.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
	private UUID id;
	private String name;
	private String password;
	private String firstName;
	private String secondName;
	private String lastName;
	private UUID departmentId;
}