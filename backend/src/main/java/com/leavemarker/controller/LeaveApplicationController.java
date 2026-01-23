package com.leavemarker.controller;

import com.leavemarker.dto.ApiResponse;
import com.leavemarker.dto.leaveapplication.LeaveApplicationRequest;
import com.leavemarker.dto.leaveapplication.LeaveApplicationResponse;
import com.leavemarker.dto.leaveapplication.LeaveApprovalRequest;
import com.leavemarker.security.UserPrincipal;
import com.leavemarker.service.LeaveApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/leave-applications")
@RequiredArgsConstructor
public class LeaveApplicationController {

    private final LeaveApplicationService leaveApplicationService;

    @PostMapping
    public ResponseEntity<ApiResponse<LeaveApplicationResponse>> applyLeave(
            @Valid @RequestBody LeaveApplicationRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        LeaveApplicationResponse response = leaveApplicationService.applyLeave(request, currentUser);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Leave application submitted successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LeaveApplicationResponse>> getLeaveApplication(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        LeaveApplicationResponse response = leaveApplicationService.getLeaveApplication(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Leave application retrieved successfully", response));
    }

    @GetMapping("/my-leaves")
    public ResponseEntity<ApiResponse<List<LeaveApplicationResponse>>> getMyLeaveApplications(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<LeaveApplicationResponse> response = leaveApplicationService.getMyLeaveApplications(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Leave applications retrieved successfully", response));
    }

    @GetMapping("/my-leaves/pending/count")
    public ResponseEntity<ApiResponse<Long>> getPendingApplicationsCount(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        long count = leaveApplicationService.getMyLeaveApplications(currentUser).stream()
                .filter(app -> "PENDING".equals(app.getStatus()))
                .count();
        return ResponseEntity.ok(ApiResponse.success("Pending applications count retrieved successfully", count));
    }

    @GetMapping("/pending-approvals/manager")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<List<LeaveApplicationResponse>>> getPendingApprovalsForManager(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<LeaveApplicationResponse> response = leaveApplicationService.getPendingApprovalsForManager(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Pending approvals retrieved successfully", response));
    }

    @GetMapping("/pending-approvals/hr")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HR_ADMIN')")
    public ResponseEntity<ApiResponse<List<LeaveApplicationResponse>>> getPendingApprovalsForHr(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<LeaveApplicationResponse> response = leaveApplicationService.getPendingApprovalsForHr(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Pending HR approvals retrieved successfully", response));
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HR_ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<LeaveApplicationResponse>>> getLeaveApplicationsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<LeaveApplicationResponse> response = leaveApplicationService.getLeaveApplicationsByDateRange(
                startDate, endDate, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Leave applications retrieved successfully", response));
    }

    @PostMapping("/{id}/approve/manager")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<LeaveApplicationResponse>> approveByManager(
            @PathVariable Long id,
            @Valid @RequestBody LeaveApprovalRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        LeaveApplicationResponse response = leaveApplicationService.approveByManager(id, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Leave application processed successfully", response));
    }

    @PostMapping("/{id}/approve/hr")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HR_ADMIN')")
    public ResponseEntity<ApiResponse<LeaveApplicationResponse>> approveByHr(
            @PathVariable Long id,
            @Valid @RequestBody LeaveApprovalRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        LeaveApplicationResponse response = leaveApplicationService.approveByHr(id, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Leave application processed successfully", response));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<LeaveApplicationResponse>> cancelLeave(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        LeaveApplicationResponse response = leaveApplicationService.cancelLeave(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Leave application cancelled successfully", response));
    }
}
