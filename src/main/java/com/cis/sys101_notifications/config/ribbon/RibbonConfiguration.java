package com.cis.sys101_notifications.config.ribbon;

import com.cis.sys101_notifications.config.AppConfig;
import com.netflix.loadbalancer.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.context.annotation.Bean;

import java.util.Collections;

@Slf4j
@RibbonClient(name = "notifications")
public class RibbonConfiguration {

	@Autowired
	private AppConfig appConfig;

	@Bean
	public IRule loadBalancingRule() {
		new WeightedResponseTimeRule();
		new AvailabilityFilteringRule();
		new WeightedResponseTimeRule();
		return new RoundRobinRule();
	}

	@Bean
	public IPing pingConfiguration(ServerList<Server> serverList) {
		String pingPath = "/actuator/health";
		IPing ping = new PingUrl(false, pingPath);
		log.info("Configuring ping URI to [{}]", pingPath);

		return ping;
	}

	@Bean
	public ServerList<Server> serverList() {
		return RibbonHelper.serverList(Collections.singletonList(new Server(appConfig.getRibbon_server_proto(),
				appConfig.getRibbon_server_host(), Integer.valueOf(appConfig.getRibbon_server_port()))));
	}
}
