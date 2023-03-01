package com.cis.sys101_notifications.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.io.Serializable;
import java.util.UUID;

@ToString
@Getter
@Setter
public class Role implements Serializable {

	private UUID id;
	private String name;
	private String description;

	public Role(String name) {
		super();
		this.name = name;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Role other = (Role) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}