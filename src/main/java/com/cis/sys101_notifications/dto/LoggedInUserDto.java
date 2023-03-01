package com.cis.sys101_notifications.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoggedInUserDto {

	private UUID departmentId;
	private List<String> roles;
	private String username;

	@Override
	public String toString() {
		return "LoggedInUserDto{" +
		"departmentId=" + departmentId +
		", roles=" + roles +
		", username='" + username + '\'' +
		'}';
	}
}