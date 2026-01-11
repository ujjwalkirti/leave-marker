package com.leavemarker.controller;

import com.leavemarker.dto.ApiResponse;
import com.leavemarker.dto.leavepolicy.LeavePolicyRequest;
import com.leavemarker.dto.leavepolicy.LeavePolicyResponse;
import com.leavemarker.security.UserPrincipal;
import com.leavemarker.service.LeavePolicyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/leave-policies")
@RequiredArgsConstructor
public class LeavePolicyController {

    private final LeavePolicyService leavePolicyService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HR_ADMIN')")
    public ResponseEntity<ApiResponse<LeavePolicyResponse>> createLeavePolicy(
            @Valid @RequestBody LeavePolicyRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        LeavePolicyResponse response = leavePolicyService.createLeavePolicy(request, currentUser);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Leave policy created successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HR_ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<LeavePolicyResponse>> getLeavePolicy(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        LeavePolicyResponse response = leavePolicyService.getLeavePolicy(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Leave policy retrieved successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HR_ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<LeavePolicyResponse>>> getAllLeavePolicies(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<LeavePolicyResponse> response = leavePolicyService.getAllLeavePolicies(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Leave policies retrieved successfully", response));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<LeavePolicyResponse>>> getActiveLeavePolicies(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<LeavePolicyResponse> response = leavePolicyService.getActiveLeavePolicies(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Active leave policies retrieved successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HR_ADMIN')")
    public ResponseEntity<ApiResponse<LeavePolicyResponse>> updateLeavePolicy(
            @PathVariable Long id,
            @Valid @RequestBody LeavePolicyRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        LeavePolicyResponse response = leavePolicyService.updateLeavePolicy(id, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Leave policy updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HR_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteLeavePolicy(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        leavePolicyService.deleteLeavePolicy(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Leave policy deleted successfully"));
    }
}
