package com.cis.sys101_notifications.dto;

import com.cis.sys101_notifications.domain.Role;
import lombok.*;

import java.util.UUID;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleDto {
	protected UUID id;
	private String name;
	private String description;
	private boolean selected;

	public RoleDto(UUID id) {
		super();
		this.id = id;
	}

	public RoleDto(Role role) {
		this.name = role.getName();
		this.id = role.getId();
		this.description = role.getDescription();
	}

	public RoleDto(UUID id, String name, String description) {
		this.id = id;
		this.name = name;
		this.description = description;
	}
}