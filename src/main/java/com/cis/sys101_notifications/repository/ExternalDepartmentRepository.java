package com.cis.sys101_notifications.repository;

import com.cis.sys101_notifications.domain.ExternalDepartment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ExternalDepartmentRepository extends JpaRepository<ExternalDepartment, UUID> {}