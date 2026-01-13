package com.leavemarker.controller;

import com.leavemarker.dto.ApiResponse;
import com.leavemarker.dto.employee.EmployeeRequest;
import com.leavemarker.dto.employee.EmployeeResponse;
import com.leavemarker.dto.employee.EmployeeUpdateRequest;
import com.leavemarker.security.UserPrincipal;
import com.leavemarker.service.EmployeeService;
import com.leavemarker.service.PlanValidationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;
    private final PlanValidationService planValidationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HR_ADMIN')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> createEmployee(
            @Valid @RequestBody EmployeeRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        // Validate employee limit based on plan
        planValidationService.validateEmployeeLimit(currentUser.getCompanyId());

        EmployeeResponse response = employeeService.createEmployee(request, currentUser);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Employee created successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getEmployee(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        EmployeeResponse response = employeeService.getEmployee(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Employee retrieved successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getAllEmployees(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<EmployeeResponse> response = employeeService.getAllEmployees(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Employees retrieved successfully", response));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getActiveEmployees(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<EmployeeResponse> response = employeeService.getActiveEmployees(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Active employees retrieved successfully", response));
    }

    @GetMapping("/active/count")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HR_ADMIN')")
    public ResponseEntity<ApiResponse<Long>> getActiveEmployeesCount(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        long count = employeeService.getActiveEmployees(currentUser).size();
        return ResponseEntity.ok(ApiResponse.success("Active employees count retrieved successfully", count));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HR_ADMIN')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        EmployeeResponse response = employeeService.updateEmployee(id, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Employee updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HR_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivateEmployee(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        employeeService.deactivateEmployee(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Employee deactivated successfully"));
    }
}
