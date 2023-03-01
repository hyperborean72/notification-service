package com.cis.sys101_notifications.config;

import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.client.KeycloakClientRequestFactory;
import org.keycloak.adapters.springsecurity.client.KeycloakRestTemplate;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

/*
* It is a stereotype to wrap the two annotations required by a Spring Security configuration class:
* @Configuration and @EnableWebSecurity.
* It also packs a third annotation required by Keycloak to scan correctly the beans configured in the Keycloak Spring Security Adapter:
* @ComponentScan(basePackageClasses = KeycloakSecurityComponents.class).
* */
@KeycloakConfiguration
public class KeycloakSecurityConfig extends KeycloakWebSecurityConfigurerAdapter {
	@Autowired
	public KeycloakClientRequestFactory keycloakClientRequestFactory;

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public KeycloakRestTemplate keycloakRestTemplate() {

		return new KeycloakRestTemplate(keycloakClientRequestFactory);
	}

	/*
	* We are registering KeycloakAuthenticationProvider with the authentication manager.
	* Keycloak will be responsible for providing authentication services.
	* */
	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) {

		KeycloakAuthenticationProvider keycloakAuthenticationProvider = keycloakAuthenticationProvider();
		keycloakAuthenticationProvider.setGrantedAuthoritiesMapper(new SimpleAuthorityMapper());
		auth.authenticationProvider(keycloakAuthenticationProvider);
	}

	/*
	* provides automatic discovery of the Keycloak configuration from the application.yml
	* */
	@Bean
	public KeycloakSpringBootConfigResolver KeycloakConfigResolver() {
		return new KeycloakSpringBootConfigResolver();
	}


	/*
	* The application that we are building, in Keycloak terms, is a public application with user interaction.
	* In this scenario, the recommended session authentication strategy is RegisterSessionAuthenticationStrategy,
	* which registers a user session after successful authentication.
	*
	* When securing a service-to-service application, instead, we would use a NullAuthenticatedSessionStrategy.
	* */
	@Bean
	@Override
	protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
		return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		super.configure(http);

		http.csrf().disable().authorizeRequests().antMatchers("/**", "/h2-console/**").permitAll();

	}
}