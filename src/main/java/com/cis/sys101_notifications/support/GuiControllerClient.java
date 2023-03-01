package com.cis.sys101_notifications.support;

import com.cis.sys101_notifications.config.FeignConfiguration;
import com.cis.sys101_notifications.dto.ActionResponse;
import com.cis.sys101_notifications.dto.RoleDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import java.util.UUID;

@FeignClient(name = "gui", configuration = FeignConfiguration.class)
public interface GuiControllerClient {

    @GetMapping(path = "api/guicontroller-auth/access/getUserInfo")
	ResponseEntity<ActionResponse> getUserByName(@RequestHeader("Authorization") String authToken, @RequestParam("userName") String userName);

    @GetMapping(path = "api/guicontroller-auth/access/roles/user/{id}")
	ResponseEntity<List<RoleDto>> getRolesByUserID(@RequestHeader("Authorization") String authToken, @PathVariable("id") UUID id);

}