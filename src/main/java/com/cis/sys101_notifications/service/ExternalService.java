package com.cis.sys101_notifications.service;

import com.cis.sys101_notifications.domain.ExternalDepartment;
import com.cis.sys101_notifications.dto.DepartmentDto;
import com.cis.sys101_notifications.repository.ExternalDepartmentRepository;
import com.cis.sys101_notifications.support.ForcesClient;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExternalService {

	private final ExternalDepartmentRepository externalDepartmentRepository;
	private final ForcesClient forcesClient;
	private final ModelMapper modelMapper;

	@Transactional
	public void syncDepartments(String authToken) {
		try {
			forcesClient.getAllDepartments(authToken).forEach(d -> externalDepartmentRepository.save(convertDeptDto(d)));
		} catch (FeignException e) {
			log.error("Ошибка синхронизации подразделений: {}", e.getLocalizedMessage());
		}
	}

	@Transactional
	public void syncDepartmentRecord(String authToken, UUID id) {
		try {
			ExternalDepartment entityToSave = convertDeptDto(forcesClient.getDepartmentById(authToken, id));
			externalDepartmentRepository.save(entityToSave);
		} catch (FeignException exception) {
			log.error("Ошибка соединения с сервисом  Силы и средства");
		} catch (Throwable exception) {
			log.error("Ошибка синхронизации подразделений");
		}
	}

	@Transactional
	public void deleteDepartmentRecord(UUID id) {
		log.info("Удаление локального подразделения  с id {} ", id);
		if (!externalDepartmentRepository.existsById(id)) {
			log.error("В таблице ext_department нет записи с id = {}", id);
			return;
		}
		externalDepartmentRepository.deleteById(id);

	}

	private ExternalDepartment convertDeptDto(DepartmentDto departmentDto) {
		return modelMapper.map(departmentDto, ExternalDepartment.class);
	}

	public DepartmentDto convertDeptEntity(ExternalDepartment departmentEntity) {
		DepartmentDto departmentDto = new DepartmentDto();
		departmentDto.setId(departmentEntity.getId());
		departmentDto.setShortName(departmentEntity.getShortName());
		return departmentDto;
	}
}