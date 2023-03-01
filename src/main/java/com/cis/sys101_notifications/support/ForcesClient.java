package com.cis.sys101_notifications.support;

import com.cis.sys101_notifications.config.FeignConfiguration;
import com.cis.sys101_notifications.dto.DepartmentDto;
import com.cis.sys101_notifications.dto.GarrisonDto;
import com.cis.sys101_notifications.dto.KeyValueObject;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "forces", configuration = FeignConfiguration.class)
public interface ForcesClient {

	@GetMapping(path = "api/forces/department/garrison/by_department")
	ResponseEntity<GarrisonDto> getGarrisonByDepartmentId(@RequestHeader("Authorization") String authToken, @RequestParam("departmentId") UUID departmentId);

	@GetMapping(path = "api/forces/department/identify/{id}")
	ResponseEntity<KeyValueObject> getDepartmentById(@RequestHeader("Authorization") String authToken, @PathVariable("id") UUID id, @RequestParam("param") String param);

	@GetMapping(path = "/api/forces/department/dropdown_list/content/{id}")
	DepartmentDto getDepartmentById(@RequestHeader("Authorization") String authToken, @PathVariable("id") UUID id);

	@GetMapping(path = "api/forces/department/departments")
	List<DepartmentDto> getAllDepartments(@RequestHeader("Authorization") String authToken);
}