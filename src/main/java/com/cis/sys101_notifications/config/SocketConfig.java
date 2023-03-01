package com.cis.sys101_notifications.config;

import com.cis.sys101_notifications.dto.LoggedInUserDto;
import com.cis.sys101_notifications.service.FeignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.keycloak.TokenVerifier;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.AccessToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
// @EnableWebSocketMessageBroker enables WebSocket message handling backed by a message broker ('simple' ie in-memory or external)
//  enableStompBrokerRelay()  enables  RabbitMQ  with STOMP support as message broker.
@EnableWebSocketMessageBroker
@Slf4j
@RequiredArgsConstructor
public class SocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final String CURRENT_DEPARTMENT = "current_department";

    public static Map<UUID, LoggedInUserDto> LoggedInUsers = new ConcurrentHashMap<>();

	@Value("${spring.rabbitmq.host}")
	private String rabbitmqHost;

	@Value("${spring.rabbitmq.username}")
	private String rabbitmqUser;

	@Value("${spring.rabbitmq.password}")
	private String rabbitmqPassword;

	private final KeycloakSpringBootProperties configuration;
	private final FeignService feignService;

	/*
	* Важно понимать, что обмен STOMP сообщениями может быть организован
	* как при помощи in-memory message broker, так и external message broker
	* в зависимости от того, какой тип message broker сконфигурирован в .configureMessageBroker
	* - enableStompBrokerRelay или
	* - enableSimpleBroker
	*
	* Также важно понимать, что Spring's approach to working with STOMP messaging
	* is to associate a controller method to the configured endpoint .
	* This is made possible through the @MessageMapping annotation.
	* */
	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry
		.addEndpoint("/notificationWebsocket")		//enabling Spring’s STOMP support on the ' /notificationWebsocket' endpoint
																															// this endpoint when prefixed with “/app”, is the endpoint that the Controller.send() method is mapped to handle.
		.setAllowedOrigins("*")																// not needed if  the client and the server use the same domain
		//.addInterceptors(new StompHandshakeInterceptor(configuration))
		.withSockJS();
		/*
	 * The default value is "true" to maximize the chance for applications to work
	 * correctly in IE 8,9 with support for cookies (and the JSESSIONID cookie in
	 * particular). However, an application can choose to set this to "false" if the use
	 * of cookies (and HTTP session) is not required.
		.setSessionCookieNeeded(true)

	* Some load balancers don't support WebSocket. This option can be used to
	* disable the WebSocket transport on the server side.
	* The default value is "true".
      		 .setWebSocketEnabled(true); */
	}

	/* NOTE:
	* destinations of the in-bound (in controller) and the out-bound(out of controller)  messages are the same
	* but the prefix '/app' for in-bound message like '/app/notificationProcessed' is replaced with '/topic' prefix
	* being sent from controller to message broker like ''/topic/notificationProcessed'
	* */
	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry
		.setApplicationDestinationPrefixes("/app")										// each message that has one of the prefixes triggers controller's method annotated with @MessageMapping
		.enableStompBrokerRelay("/topic", "/queue")	// each message that has '/topic' in the URL is sent to Rabbit's 'topic' after being processed by controller's method annotated with @MessageMapping
		.setRelayHost(rabbitmqHost)
		.setRelayPort(61613)
		.setClientLogin(rabbitmqUser)
		.setClientPasscode(rabbitmqPassword);
	}

	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {

		registration.interceptors(new ChannelInterceptor() {
			@Override
			public Message<?> preSend(Message<?> message, MessageChannel channel) {

				String current_department;
				List<String>  roles = new ArrayList<>();

				StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                log.info("StompHeaderAccessor: {}", accessor.toString());

				String headersAsString = Arrays.asList(message.getHeaders()).toString();
				log.info("Message: message.payload - {}, message.headers - {}", message.getPayload(), headersAsString);

				/* substringBetween извлечет строку до 1-ой закрывающей скобки после открывающей последовательности */
				String tokenAsString = StringUtils.substringBetween(headersAsString, "token=[", "]");
				String sessionIdAsString = StringUtils.substringBetween(headersAsString, "sessionId=[", "]");

				log.info("Token: {}", tokenAsString);
				log.info("SessionId: {}", sessionIdAsString);

				if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    try {
                        AccessToken token = TokenVerifier.create(tokenAsString, AccessToken.class).getToken();
                        Map<String, Object> otherClaims = token.getOtherClaims();
                        log.info("Claims: {}",  Arrays.asList(otherClaims));

                        String username = token.getPreferredUsername();
                        log.info("Preferred username: {}",  username);

                        if (ObjectUtils.isNotEmpty(otherClaims) && ObjectUtils.isNotEmpty(otherClaims.get(CURRENT_DEPARTMENT))) {
                            current_department = otherClaims.get(CURRENT_DEPARTMENT).toString();
                            log.info("Current department: {}", current_department);

                            LoggedInUserDto loggedInUserDto = new LoggedInUserDto();
                            loggedInUserDto.setDepartmentId(UUID.fromString(current_department));
                            loggedInUserDto.setUsername(username);
							roles = feignService.getUserRoles(ServiceConstants.BEARER + tokenAsString, username);
							log.info("User roles: {}", roles);
							loggedInUserDto.setRoles(roles);

							LoggedInUsers.putIfAbsent(UUID.fromString(sessionIdAsString), loggedInUserDto);
							log.info("LoggedInUsers: {}", Arrays.asList(LoggedInUsers));
                        }
                    } catch (VerificationException e) {
                        log.error("VerificationException: {}", e.getMessage());
                    }
                }

				if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
					LoggedInUserDto removedLoggedInUserDto = LoggedInUsers.remove(UUID.fromString(sessionIdAsString));
					log.info("User disconnected: {}", removedLoggedInUserDto);
				}

				return message;
			}
		});
	}
}