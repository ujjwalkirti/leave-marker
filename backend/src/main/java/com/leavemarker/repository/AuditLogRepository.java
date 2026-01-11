package com.leavemarker.repository;

import com.leavemarker.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByCompanyIdOrderByCreatedAtDesc(Long companyId);

    List<AuditLog> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);

    List<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, Long entityId);
}
