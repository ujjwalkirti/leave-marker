package com.leavemarker.service;

import com.leavemarker.entity.AuditLog;
import com.leavemarker.entity.Company;
import com.leavemarker.entity.Employee;
import com.leavemarker.repository.AuditLogRepository;
import com.leavemarker.repository.CompanyRepository;
import com.leavemarker.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public void logAction(Long companyId, Long employeeId, String action,
                         String entityType, Long entityId, String oldValue,
                         String newValue, String ipAddress) {
        Company company = companyRepository.findById(companyId).orElse(null);
        Employee employee = employeeId != null ?
                employeeRepository.findById(employeeId).orElse(null) : null;

        AuditLog log = AuditLog.builder()
                .company(company)
                .employee(employee)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .oldValue(oldValue)
                .newValue(newValue)
                .ipAddress(ipAddress)
                .build();

        auditLogRepository.save(log);
    }

    @Transactional
    public void logLeaveApproval(Long companyId, Long approverId, Long leaveId,
                                String action, String previousStatus, String newStatus) {
        logAction(companyId, approverId, action, "LeaveApplication", leaveId,
                previousStatus, newStatus, null);
    }

    @Transactional
    public void logPolicyChange(Long companyId, Long employeeId, Long policyId,
                               String action, String oldValue, String newValue) {
        logAction(companyId, employeeId, action, "LeavePolicy", policyId,
                oldValue, newValue, null);
    }

    @Transactional
    public void logEmployeeChange(Long companyId, Long performerId, Long employeeId,
                                 String action, String oldValue, String newValue) {
        logAction(companyId, performerId, action, "Employee", employeeId,
                oldValue, newValue, null);
    }
}
