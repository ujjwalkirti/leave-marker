package com.leavemarker.controller;

import java.time.Year;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.leavemarker.dto.ApiResponse;
import com.leavemarker.dto.leavebalance.LeaveBalanceResponse;
import com.leavemarker.entity.Employee;
import com.leavemarker.entity.LeaveBalance;
import com.leavemarker.repository.EmployeeRepository;
import com.leavemarker.security.UserPrincipal;
import com.leavemarker.service.LeaveBalanceService;

import lombok.RequiredArgsConstructor;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/leave-balance")
@RequiredArgsConstructor
public class LeaveBalanceController {

    private final LeaveBalanceService leaveBalanceService;
    private final EmployeeRepository employeeRepository;

    @GetMapping("/my-balance")
    public ResponseEntity<ApiResponse<List<LeaveBalanceResponse>>> getMyLeaveBalance(
            @RequestParam(required = false) Integer year,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        Integer targetYear = year != null ? year : Year.now().getValue();
        List<LeaveBalance> balances = leaveBalanceService.getEmployeeBalances(
            currentUser.getId(),
            targetYear
        );

        // Convert to DTOs to avoid Hibernate proxy serialization issues
        List<LeaveBalanceResponse> response = balances.stream()
            .map(balance -> LeaveBalanceResponse.builder()
                .id(balance.getId())
                .leaveType(balance.getLeaveType())
                .year(balance.getYear())
                .totalQuota(balance.getTotalQuota())
                .used(balance.getUsed())
                .pending(balance.getPending())
                .available(balance.getAvailable())
                .carriedForward(balance.getCarriedForward())
                .build())
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(
            "Leave balances retrieved successfully",
            response
        ));
    }

    @PostMapping("/initialize")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HR_ADMIN')")
    public ResponseEntity<ApiResponse<String>> initializeLeaveBalances(
            @RequestParam(required = false) Integer year,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        Integer targetYear = year != null ? year : Year.now().getValue();

        // Get all active employees in the company
        List<Employee> employees = employeeRepository
            .findByCompanyIdAndDeletedFalse(currentUser.getCompanyId());

        int initializedCount = 0;
        for (Employee employee : employees) {
            leaveBalanceService.initializeLeaveBalanceForEmployee(
                employee.getId(),
                targetYear
            );
            initializedCount++;
        }

        String message = String.format(
            "Leave balances initialized for %d employees for year %d",
            initializedCount,
            targetYear
        );

        return ResponseEntity.ok(ApiResponse.success(message, message));
    }
}
