package com.leavemarker.repository;

import com.leavemarker.entity.LeavePolicy;
import com.leavemarker.enums.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeavePolicyRepository extends JpaRepository<LeavePolicy, Long> {

    List<LeavePolicy> findByCompanyIdAndDeletedFalse(Long companyId);

    List<LeavePolicy> findByCompanyIdAndActiveAndDeletedFalse(Long companyId, Boolean active);

    Optional<LeavePolicy> findByCompanyIdAndLeaveTypeAndDeletedFalse(Long companyId, LeaveType leaveType);

    boolean existsByCompanyIdAndLeaveTypeAndDeletedFalse(Long companyId, LeaveType leaveType);

    long countByCompanyIdAndDeletedFalse(Long companyId);

    long countByCompanyIdAndActiveAndDeletedFalse(Long companyId, Boolean active);
}
