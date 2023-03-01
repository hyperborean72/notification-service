package com.cis.sys101_notifications.domain;

import lombok.Data;
import javax.persistence.*;
import java.util.UUID;

@Data
@MappedSuperclass
public class AbstractEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private UUID id;
}