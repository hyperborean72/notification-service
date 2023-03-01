package com.cis.sys101_notifications.domain;

import com.cis.sys101_notifications.domain.enums.EventType;
import lombok.Data;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

import static javax.persistence.EnumType.STRING;

@Entity
@Table(name = "notifications")
@Data
public class Notification extends AbstractEntity{

	@Enumerated(STRING)
	@Column(name = "event_type")
	private EventType eventType;

	@Column(name = "fire_station_sender")
	private String unitShortName;

	@Column(name = "created_at", updatable = false)
	private Instant createdAt = Instant.now();

	@Column(name = "processed_at")
	private Instant processedAt ;

	@Column(name = "processed_by")
	private UUID processedBy ;

	@Column
	private String  result;

	@Column(name = "is_processed")
	private boolean isProcessed;

	@Column
	private String  content;
}