package com.cis.sys101_notifications.config.ribbon;

import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerList;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class RibbonHelper {
    public static ServerList<Server> serverList(List<Server> incomingServerList) {
        return new ServerList<Server>() {
            @Override
            public List<Server> getUpdatedListOfServers() {
                log.info("Возврат обновленного списка серверов [{}]", incomingServerList);
                return incomingServerList;
            }

            @Override
            public List<Server> getInitialListOfServers() {
                return incomingServerList;
            }
        };
    }
}
