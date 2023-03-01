package com.cis.sys101_notifications.config;

import org.keycloak.KeycloakSecurityContext;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
public final class ServiceConstants {

	public static final String DB_SCHEME = "sys101_notifications";
	public static final String BEARER = "Bearer ";

	public static String getToken(HttpServletRequest request) {
		KeycloakSecurityContext session = (KeycloakSecurityContext) request
		.getAttribute(KeycloakSecurityContext.class.getName());

		String token = "Bearer " + session.getTokenString();
		return token;
	}
}