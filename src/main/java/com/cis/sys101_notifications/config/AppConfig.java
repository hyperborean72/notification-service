package com.cis.sys101_notifications.config;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@ToString
@PropertySource("classpath:application.yml")
@ConfigurationProperties("app")
@Configuration // пишут, что  not needed in  Spring Boot 2.2 , однако без неё наш бин не формируется
public class AppConfig {

	private Map<String, List<String>> templates;

	private Map<String, List<String>> recipients;

	private String ribbon_server_host;
	private String ribbon_server_port;
	private String ribbon_server_proto;

	private Map<String, String> eventExchanges;
	private String notificationExchange;
}