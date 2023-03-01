package com.cis.sys101_notifications.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActionResponse {
	public int code;
	public UserDto content;
}
