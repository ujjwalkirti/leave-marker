package com.leavemarker.controller;

import com.leavemarker.dto.ApiResponse;
import com.leavemarker.dto.attendance.AttendanceCorrectionRequest;
import com.leavemarker.dto.attendance.AttendancePunchRequest;
import com.leavemarker.dto.attendance.AttendanceResponse;
import com.leavemarker.enums.AttendanceStatus;
import com.leavemarker.security.UserPrincipal;
import com.leavemarker.service.AttendanceService;
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
@RequestMapping("/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/punch")
    public ResponseEntity<ApiResponse<AttendanceResponse>> punchInOut(
            @Valid @RequestBody AttendancePunchRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        AttendanceResponse response = attendanceService.punchInOut(request, currentUser);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Attendance recorded successfully", response));
    }

    @GetMapping("/today")
    public ResponseEntity<ApiResponse<AttendanceResponse>> getTodayAttendance(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        AttendanceResponse response = attendanceService.getTodayAttendance(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Today's attendance retrieved successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AttendanceResponse>> getAttendance(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        AttendanceResponse response = attendanceService.getAttendance(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Attendance retrieved successfully", response));
    }

    @GetMapping("/my-attendance")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getMyAttendance(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<AttendanceResponse> response = attendanceService.getMyAttendance(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Attendance records retrieved successfully", response));
    }

    @GetMapping("/my-attendance/date-range")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getMyAttendanceByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<AttendanceResponse> response = attendanceService.getMyAttendanceByDateRange(startDate, endDate, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Attendance records retrieved successfully", response));
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HR_ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getAttendanceByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<AttendanceResponse> response = attendanceService.getAttendanceByDateRange(startDate, endDate, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Attendance records retrieved successfully", response));
    }

    @PostMapping("/{id}/request-correction")
    public ResponseEntity<ApiResponse<AttendanceResponse>> requestCorrection(
            @PathVariable Long id,
            @Valid @RequestBody AttendanceCorrectionRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        AttendanceResponse response = attendanceService.requestCorrection(id, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Correction request submitted successfully", response));
    }

    @PostMapping("/{id}/approve-correction")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HR_ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<AttendanceResponse>> approveCorrection(
            @PathVariable Long id,
            @Valid @RequestBody AttendanceCorrectionRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        AttendanceResponse response = attendanceService.approveCorrection(id, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Correction approved successfully", response));
    }

    @PostMapping("/{id}/reject-correction")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HR_ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<AttendanceResponse>> rejectCorrection(
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        AttendanceResponse response = attendanceService.rejectCorrection(id, reason, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Correction rejected successfully", response));
    }

    @GetMapping("/pending-corrections")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HR_ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getPendingCorrections(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<AttendanceResponse> response = attendanceService.getPendingCorrections(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Pending corrections retrieved successfully", response));
    }

    @PostMapping("/mark")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HR_ADMIN')")
    public ResponseEntity<ApiResponse<AttendanceResponse>> markAttendance(
            @RequestParam Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam AttendanceStatus status,
            @RequestParam(required = false) String remarks,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        AttendanceResponse response = attendanceService.markAttendance(employeeId, date, status, remarks, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Attendance marked successfully", response));
    }
}
