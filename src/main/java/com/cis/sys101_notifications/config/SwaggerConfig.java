package com.cis.sys101_notifications.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import springfox.documentation.builders.*;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger.web.SecurityConfigurationBuilder;

import java.util.Arrays;
import java.util.Collections;

/*В Spring Boot , кажется, не требуется наследовать WebMvcConfigurationSupport и переопределять addResourceHandlers*/
@Configuration
public class SwaggerConfig extends WebMvcConfigurationSupport {

	@Value("${keycloak.realm}")
	private String realm;

	@Value("${swagger.auth.token-url}")
	private String token_url;

	@Value("${swagger.auth.client-secret}")
	private String client_secret;

	@Value("${swagger.auth.client-id}")
	private String client_id;

	@Value("${swagger.auth.auth-url}")
	private String auth_url;

	@Bean
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2)
		.select()
		.apis(RequestHandlerSelectors.withClassAnnotation(RestController.class))
		.paths(PathSelectors.any())
		.build().securitySchemes(Collections.singletonList(securityScheme()))
		.apiInfo(metaData())
		.securityContexts(Collections.singletonList(securityContext()));
	}

	private SecurityContext securityContext() {
		return SecurityContext.builder()
		.securityReferences(Collections.singletonList(new SecurityReference("spring_oauth", scopes())))
		.forPaths(PathSelectors.regex("/.*"))
		.build();
	}

	@Bean
	public SecurityConfiguration security() {

		return SecurityConfigurationBuilder
		.builder()
		.realm(realm)
		.clientId(client_id)
		.clientSecret(client_secret)
		.appName("sys101_notifications")
		.scopeSeparator(" ")
		.useBasicAuthenticationWithAccessCodeGrant(true)
		.build();
	}

	private SecurityScheme securityScheme() {
		GrantType grantType = new AuthorizationCodeGrantBuilder()
		.tokenEndpoint(new TokenEndpoint(token_url, "access_token"))
		.tokenRequestEndpoint(new TokenRequestEndpoint(auth_url, client_id, client_secret))
		.build();

		return new OAuthBuilder()
		.name("spring_oauth")
		.grantTypes(Collections.singletonList(grantType))
		.scopes(Arrays.asList(scopes()))
		.build();
	}

	private AuthorizationScope[] scopes() {
		return new AuthorizationScope[]{};
	}

	private ApiInfo metaData() {
		return new ApiInfoBuilder()
		.title(SWAGGER_TITLE)
		.description(SWAGGER_DESCRIPTION)
		.contact(new Contact("КИ Системы", "http://c-i-systems.com", "info@c-i-systems.com"))
		.build();
	}

	@Override
	protected void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
		registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");

	}

	public static final String SWAGGER_TITLE = "Сервис уведомлений";
	public static final String SWAGGER_DESCRIPTION = "Сервис уведомлений представляет собой middleware (промежуточное ПО), " +
	"слушающее сообщения об определенных событиях из  очередей  RabbitMQ,\n " +
	"фиксирующее в БД факт наступления события,.\n" +
	"формирующее в ответ на сообщение о событии  ряд уведомлений согласно приоритетам, \n" +
	"реализующее рассылку уведомлений получателям согласно их роли,\n" +
	"фиксирующее в БД результат обработки уведомления с приоритетом \"Критическое\" ";
}