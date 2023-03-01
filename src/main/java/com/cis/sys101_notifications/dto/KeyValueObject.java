package com.cis.sys101_notifications.dto;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KeyValueObject  implements Serializable {
		private static final long serialVersionUID = 1L;

		private UUID key;
		private String value;
}