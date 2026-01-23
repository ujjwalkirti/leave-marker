package com.leavemarker.controller;

import com.leavemarker.dto.ApiResponse;
import com.leavemarker.dto.attendance.AttendanceCorrectionRequest;
import com.leavemarker.dto.attendance.AttendancePunchRequest;
import com.leavemarker.dto.attendance.AttendanceResponse;
import com.leavemarker.enums.AttendanceStatus;
import com.leavemarker.security.UserPrincipal;
import com.leavemarker.service.AttendanceService;
import com.leavemarker.service.PlanValidationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final PlanValidationService planValidationService;

    @PostMapping("/punch")
    public ResponseEntity<ApiResponse<AttendanceResponse>> punchInOut(
            @Valid @RequestBody AttendancePunchRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        // Validate attendance management access
        planValidationService.validateAttendanceManagementAccess(currentUser.getCompanyId());

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

    @GetMapping("/my-attendance/rate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMyAttendanceRate(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        // Validate attendance rate analytics access
        planValidationService.validateAttendanceRateAnalyticsAccess(currentUser.getCompanyId());

        YearMonth targetMonth = (year != null && month != null)
            ? YearMonth.of(year, month)
            : YearMonth.now();

        LocalDate startDate = targetMonth.atDay(1);
        LocalDate endDate = targetMonth.atEndOfMonth();

        List<AttendanceResponse> records = attendanceService.getMyAttendanceByDateRange(startDate, endDate, currentUser);

        long presentDays = records.stream()
                .filter(r -> AttendanceStatus.PRESENT.name().equals(r.getStatus()))
                .count();

        long totalWorkingDays = records.size();

        double attendanceRate = totalWorkingDays > 0
            ? (presentDays * 100.0 / totalWorkingDays)
            : 0.0;

        Map<String, Object> stats = new HashMap<>();
        stats.put("attendanceRate", Math.round(attendanceRate * 10.0) / 10.0);
        stats.put("presentDays", presentDays);
        stats.put("totalWorkingDays", totalWorkingDays);
        stats.put("month", targetMonth.getMonthValue());
        stats.put("year", targetMonth.getYear());

        return ResponseEntity.ok(ApiResponse.success("Attendance rate retrieved successfully", stats));
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
