package com.cis.sys101_notifications;

import com.cis.sys101.util.RabbitCommand;
import com.cis.sys101_notifications.config.AppConfig;
import com.cis.sys101_notifications.dto.Token;
import com.cis.sys101_notifications.service.ExternalService;
import com.cis.sys101_notifications.service.RabbitClient;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.authorization.client.Configuration;
import org.keycloak.representations.idm.authorization.AuthorizationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.PropertySource;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static org.keycloak.representations.idm.CredentialRepresentation.SECRET;

@SpringBootApplication
@PropertySource("classpath:application.yml")
@EnableEurekaClient
@EnableFeignClients("com.cis.sys101_notifications")
@EnableSwagger2
@Slf4j
@RequiredArgsConstructor
public class NotificationApplication implements CommandLineRunner {

	@Value("${keycloak.resource}")
	private String clientid;

	@Value("${keycloak.credentials.secret}")
	private String secret;

	@Value("${keycloak.realm}")
	private String realmConfig;

	@Value("${keycloak.auth-server-url}")
	private String authURL;

	@Value("${app.department_exchange}")
	private String departmentExchange;

	@Value("${app.department_queue}")
	private String departmentQueue;

	private static final String sys101_user = "sys101";

	private static final String sys101_password = "20503401-da53-4549-8da5-e2a5f059e34f";

    private final AppConfig appConfig;
	private final RabbitClient  rabbitClient;
	private final ExternalService externalService;

	public static void main(String[] args) {
		SpringApplication.run(NotificationApplication.class, args);
	}

	@Override
	public void run(String... args) throws IOException, URISyntaxException {

		String token = getToken();
		externalService.syncDepartments(token);

		Channel syncChannel = rabbitClient.initSyncChannel();
		rabbitClient.initNotificationExchange();

		rabbitClient.initSyncListener(syncChannel, departmentQueue, departmentExchange, "",
		rabbitMessage -> {
			if (rabbitMessage.getCommand().equals(RabbitCommand.UPDATE_ALL)) {
				externalService.syncDepartments(token);
			}
			if ((rabbitMessage.getCommand().equals(RabbitCommand.UPDATE) || rabbitMessage.getCommand().equals(RabbitCommand.SAVE))
			&& rabbitMessage.getId() != null) {
				externalService.syncDepartmentRecord(token, rabbitMessage.getId());
			}
			if (rabbitMessage.getCommand().equals(RabbitCommand.DELETE) && rabbitMessage.getId() != null) {
				externalService.deleteDepartmentRecord(rabbitMessage.getId());
			}
		});

		appConfig.getEventExchanges().forEach((k, v) -> rabbitClient.initEventListener(k, v, ""));
	}

	private String getToken() throws IOException, URISyntaxException {

		Map<String, Object> clientCredentials = new HashMap<>();

		clientCredentials.put(SECRET, secret);

		Configuration conf = new Configuration(authURL, realmConfig, clientid, clientCredentials, null);

		AuthzClient authzClient = AuthzClient.create(conf);

		AuthorizationResponse response = authzClient.authorization(sys101_user, sys101_password).authorize();

		String rpt = response.getToken();

		return "Bearer " + new Token(rpt).getAccess_token();
	}
}