package com.cis.sys101_notifications.domain;

import com.cis.sys101_notifications.config.ServiceConstants;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(schema = ServiceConstants.DB_SCHEME, name = "ext_department")
public class ExternalDepartment {
	@Id
	private UUID id;

	private String shortName;
}