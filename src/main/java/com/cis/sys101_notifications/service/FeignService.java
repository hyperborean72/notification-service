package com.cis.sys101_notifications.service;

import com.cis.sys101_notifications.dto.ActionResponse;
import com.cis.sys101_notifications.dto.GarrisonDto;
import com.cis.sys101_notifications.dto.RoleDto;
import com.cis.sys101_notifications.support.ForcesClient;
import com.cis.sys101_notifications.support.GuiControllerClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class FeignService {

	private ForcesClient forcesClient;
	private GuiControllerClient guiControllerClient;

	public Optional<GarrisonDto> getGarrisonByDepartmentId(String token, UUID id) {
		try {
			ResponseEntity<GarrisonDto> response = forcesClient.getGarrisonByDepartmentId(token, id);

			if (ObjectUtils.isEmpty(response))
				return Optional.empty();
			org.springframework.http.HttpStatus code = response.getStatusCode();
			if (code.equals(org.springframework.http.HttpStatus.OK)) {
				GarrisonDto result = response.getBody();
				return Optional.ofNullable(result);
			} else {
				return Optional.empty();
			}
		} catch (Exception ex) {

			log.error("Feign exception: " + ex.getMessage());

			return Optional.empty();
		}
	}

	public List<String> getUserRoles (String token, String userName){
		UUID userId = guiControllerClient.getUserByName(token, userName).getBody().getContent().getId();
		log.info("UserId by username: {}, {}", userId, userName);
		List<RoleDto> roleDtoList = guiControllerClient.getRolesByUserID(token, userId).getBody();
		return roleDtoList.stream()
			.map(RoleDto::getName)
			.collect(Collectors.toList());
	}
}