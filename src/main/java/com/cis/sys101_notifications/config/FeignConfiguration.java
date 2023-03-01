package com.cis.sys101_notifications.config;

import org.springframework.context.annotation.Bean;

import feign.Logger;
import feign.Request;
import feign.RequestInterceptor;
import feign.Retryer;
import feign.auth.BasicAuthRequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class FeignConfiguration {

	/**
	 * Logging levels are BASIC, FULL, HEADERS, NONE
	 *
	 * @return Logger.Level
	 */
	@Bean
	public Logger.Level configureLogLevel() {
		return Logger.Level.BASIC;
	}

	/**
	 * Request.Options allows you to configure the connection and read timeout
	 * values that will be used by the client for each request
	 * 
	 * @return Request.Options
	 */
	@Bean
	public Request.Options timeoutConfiguration() {

		return new Request.Options(5000, 30000);
	}

	/**
	 * Request interceptor adds HTTP header for basic auth using the values supplied
	 *
	 * @return BasicAuthRequestInterceptor
	 */
	@Bean
	public BasicAuthRequestInterceptor basicAuthRequestInterceptor() {

		return new BasicAuthRequestInterceptor("user", "password");
	}

	/**
	 * An example of a custom RequestInterceptor. In this instance we add a custom
	 * header. This is a common enough use case for a request header
	 * 
	 * @return RequestInterceptor
	 */
	@Bean
	public RequestInterceptor headerRequestInterceptor() {

		return template -> {

			log.info("Adding header [testHeader / testHeaderValue] to request");
			template.header("testHeader", "testHeaderValue");
		};
	}

	/**
	 * Default Retryer will retry 5 times, backing off (exponentially) between
	 * retries. You can provide your own retry logic by implementing the Retry
	 * interface if you need some specific behaviour.
	 * 
	 * @return Retryer
	 */
	@Bean
	public Retryer retryer() {

		return new Retryer.Default(1000, 8000, 3);
	}
}