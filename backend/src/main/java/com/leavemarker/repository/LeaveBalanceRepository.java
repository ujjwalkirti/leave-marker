package com.leavemarker.repository;

import com.leavemarker.entity.LeaveBalance;
import com.leavemarker.enums.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {

    List<LeaveBalance> findByEmployeeIdAndDeletedFalse(Long employeeId);

    List<LeaveBalance> findByEmployeeIdAndYearAndDeletedFalse(Long employeeId, Integer year);

    Optional<LeaveBalance> findByEmployeeIdAndLeaveTypeAndYearAndDeletedFalse(Long employeeId, LeaveType leaveType, Integer year);

    List<LeaveBalance> findByEmployeeCompanyIdAndYearAndDeletedFalse(Long companyId, Integer year);
}
