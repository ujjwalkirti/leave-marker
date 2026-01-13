package com.leavemarker.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.leavemarker.entity.Employee;
import com.leavemarker.entity.LeaveBalance;
import com.leavemarker.entity.LeavePolicy;
import com.leavemarker.enums.LeaveType;
import com.leavemarker.exception.ResourceNotFoundException;
import com.leavemarker.repository.EmployeeRepository;
import com.leavemarker.repository.LeaveBalanceRepository;
import com.leavemarker.repository.LeavePolicyRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LeaveBalanceService {

    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeavePolicyRepository leavePolicyRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public void initializeLeaveBalanceForEmployee(Long employeeId, Integer year) {
        Employee employee = employeeRepository.findByIdAndDeletedFalse(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        List<LeavePolicy> policies = leavePolicyRepository
                .findByCompanyIdAndActiveAndDeletedFalse(employee.getCompany().getId(), true);

        for (LeavePolicy policy : policies) {
            Optional<LeaveBalance> existingBalance = leaveBalanceRepository
                    .findByEmployeeIdAndLeaveTypeAndYearAndDeletedFalse(employeeId, policy.getLeaveType(), year);

            if (existingBalance.isEmpty()) {
                LeaveBalance balance = LeaveBalance.builder()
                        .employee(employee)
                        .leaveType(policy.getLeaveType())
                        .year(year)
                        .totalQuota(policy.getAnnualQuota().doubleValue())
                        .used(0.0)
                        .pending(0.0)
                        .available(policy.getAnnualQuota().doubleValue())
                        .carriedForward(0.0)
                        .build();
                leaveBalanceRepository.save(balance);
            }
        }
    }

    @Transactional
    public void processMonthlyAccrual(Long companyId, Integer year, Integer month) {
        List<LeavePolicy> policies = leavePolicyRepository
                .findByCompanyIdAndActiveAndDeletedFalse(companyId, true);

        for (LeavePolicy policy : policies) {
            if (policy.getMonthlyAccrual() > 0) {
                List<LeaveBalance> balances = leaveBalanceRepository
                        .findByEmployeeCompanyIdAndYearAndDeletedFalse(companyId, year);

                for (LeaveBalance balance : balances) {
                    if (balance.getLeaveType() == policy.getLeaveType()) {
                        double newTotal = balance.getTotalQuota() + policy.getMonthlyAccrual();
                        balance.setTotalQuota(newTotal);
                        balance.setAvailable(newTotal - balance.getUsed() - balance.getPending());
                        leaveBalanceRepository.save(balance);
                    }
                }
            }
        }
    }

    @Transactional
    public void processYearEndCarryForward(Long companyId, Integer currentYear) {
        Integer nextYear = currentYear + 1;
        List<LeavePolicy> policies = leavePolicyRepository
                .findByCompanyIdAndActiveAndDeletedFalse(companyId, true);

        for (LeavePolicy policy : policies) {
            if (policy.getCarryForward()) {
                List<LeaveBalance> currentYearBalances = leaveBalanceRepository
                        .findByEmployeeCompanyIdAndYearAndDeletedFalse(companyId, currentYear);

                for (LeaveBalance currentBalance : currentYearBalances) {
                    if (currentBalance.getLeaveType() == policy.getLeaveType()) {
                        double availableToCarry = Math.min(
                                currentBalance.getAvailable(),
                                policy.getMaxCarryForward().doubleValue()
                        );

                        Optional<LeaveBalance> nextYearBalance = leaveBalanceRepository
                                .findByEmployeeIdAndLeaveTypeAndYearAndDeletedFalse(
                                        currentBalance.getEmployee().getId(),
                                        policy.getLeaveType(),
                                        nextYear
                                );

                        if (nextYearBalance.isPresent()) {
                            LeaveBalance balance = nextYearBalance.get();
                            balance.setCarriedForward(availableToCarry);
                            balance.setTotalQuota(balance.getTotalQuota() + availableToCarry);
                            balance.setAvailable(balance.getAvailable() + availableToCarry);
                            leaveBalanceRepository.save(balance);
                        }
                    }
                }
            }
        }
    }

    public LeaveBalance getLeaveBalance(Long employeeId, LeaveType leaveType, Integer year) {
        return leaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeAndYearAndDeletedFalse(employeeId, leaveType, year)
                .orElseThrow(() -> new ResourceNotFoundException("Leave balance not found"));
    }

    public List<LeaveBalance> getEmployeeBalances(Long employeeId, Integer year) {
        return leaveBalanceRepository.findByEmployeeIdAndYearAndDeletedFalse(employeeId, year);
    }

    @Transactional
    public void deductLeave(Long employeeId, LeaveType leaveType, Integer year, Double days, boolean isPending) {
        LeaveBalance balance = getLeaveBalance(employeeId, leaveType, year);

        if (isPending) {
            balance.setPending(balance.getPending() + days);
        } else {
            balance.setUsed(balance.getUsed() + days);
        }
        balance.setAvailable(balance.getTotalQuota() - balance.getUsed() - balance.getPending());
        leaveBalanceRepository.save(balance);
    }

    @Transactional
    public void revertPendingLeave(Long employeeId, LeaveType leaveType, Integer year, Double days) {
        LeaveBalance balance = getLeaveBalance(employeeId, leaveType, year);
        balance.setPending(balance.getPending() - days);
        balance.setAvailable(balance.getTotalQuota() - balance.getUsed() - balance.getPending());
        leaveBalanceRepository.save(balance);
    }

    @Transactional
    public void approvePendingLeave(Long employeeId, LeaveType leaveType, Integer year, Double days) {
        LeaveBalance balance = getLeaveBalance(employeeId, leaveType, year);
        balance.setPending(balance.getPending() - days);
        balance.setUsed(balance.getUsed() + days);
        balance.setAvailable(balance.getTotalQuota() - balance.getUsed() - balance.getPending());
        leaveBalanceRepository.save(balance);
    }

    public Double calculateLOPDays(Long employeeId, Integer year, Integer month) {
        List<LeaveBalance> balances = leaveBalanceRepository
                .findByEmployeeIdAndYearAndDeletedFalse(employeeId, year);

        double totalLOP = 0.0;
        for (LeaveBalance balance : balances) {
            if (balance.getLeaveType() == LeaveType.LOSS_OF_PAY) {
                totalLOP += balance.getUsed();
            }
        }

        return totalLOP;
    }
}
