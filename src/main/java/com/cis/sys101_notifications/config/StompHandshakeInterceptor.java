package com.cis.sys101_notifications.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.rotation.AdapterTokenVerifier;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.keycloak.common.VerificationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.lang.Nullable;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
public class StompHandshakeInterceptor implements HandshakeInterceptor {

	private final KeycloakSpringBootProperties configuration;

	@Override
	public boolean beforeHandshake(ServerHttpRequest req, ServerHttpResponse resp, WebSocketHandler h, Map<String, Object> atts) {
		/* вариант чтения https://stackoverflow.com/questions/50573461/spring-websockets-authentication-with-spring-security-and-keycloak*/
		List<String> protocols = req.getHeaders().get("Sec-WebSocket-Protocol");
		log.info("Sec-WebSocket-Protocol header: {}", protocols);
		if (protocols != null) {
			protocols.forEach(System.out::println);
			try {
				String token = protocols.get(0).split(", ")[2];
				log.info("Token: {}", token);
				log.info("KeycloakSpringBootProperties: credentials - {}, auth-server - {}, realm - {}, config - {}",
				configuration.getCredentials(), configuration.getAuthServerUrl(), configuration.getRealm(), Collections.singletonList(configuration.getConfig()));
				AdapterTokenVerifier.verifyToken(token, KeycloakDeploymentBuilder.build(configuration));
				resp.setStatusCode(HttpStatus.SWITCHING_PROTOCOLS);
				log.info("token valid");
			} catch (IndexOutOfBoundsException e) {
				resp.setStatusCode(HttpStatus.UNAUTHORIZED);
				return false;
			}
			catch (VerificationException e) {
				resp.setStatusCode(HttpStatus.FORBIDDEN);
				log.error(e.getMessage());
				return false;
			}
		}

		log.info("req.getHeaders() - {}", Collections.singletonList(req.getHeaders()));
		log.info("KeycloakSpringBootProperties: credentials - {}, auth-server - {}, realm - {}, config - {},  config [not converted] - {}",
			configuration.getCredentials(), configuration.getAuthServerUrl(), configuration.getRealm(), Arrays.asList(configuration.getConfig()), configuration.getConfig());
		// Collections.singletonList(configuration.getConfig())
		log.info("KeycloakSpringBootProperties full - {}", configuration.toString());

		return true;
	}

	@Override
	public void afterHandshake(ServerHttpRequest rq, ServerHttpResponse rp, WebSocketHandler h, @Nullable Exception e) {}
}

/* здесь:
https://www.thomasvitale.com/spring-security-keycloak/
https://stackoverflow.com/questions/57787768/example-keycloak-spring-boot-app-fails-to-find-bean-keycloakspringbootconfigreso

отмечается:
"Starting from Keycloak Spring Boot Adapter 7.0.0, due to some issues, the automatic discovery of the Keycloak configuration from the application.properties (or application.yml) file will not work.
 To overcome this problem, we need to define a KeycloakSpringBootConfigResolver bean explicitly in a @Configuration class.

@Configuration
public class KeycloakConfig {

    @Bean
    public KeycloakSpringBootConfigResolver keycloakConfigResolver() {
        return new KeycloakSpringBootConfigResolver();
    }
}"

Но поскольку в KeycloakSecurityConfig (@KeycloakConfiguration) мы как раз определили
@Bean
	public KeycloakSpringBootConfigResolver KeycloakConfigResolver() {
		return new KeycloakSpringBootConfigResolver();
	}

 надеюсь, что свойства подключения к KeyCloak прочитаны из application.yml  в  KeycloakSpringBootProperties
* */