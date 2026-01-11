package com.leavemarker.controller;

import com.leavemarker.security.UserPrincipal;
import com.leavemarker.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HR_ADMIN', 'MANAGER')")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/leave-balance")
    public ResponseEntity<byte[]> generateLeaveBalanceReport(
            @RequestParam Integer year,
            @RequestParam(defaultValue = "excel") String format,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        byte[] report = reportService.generateLeaveBalanceReport(year, currentUser, format);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(getMediaType(format));
        headers.setContentDispositionFormData("attachment",
                "leave_balance_report_" + year + "." + format);

        return new ResponseEntity<>(report, headers, HttpStatus.OK);
    }

    @GetMapping("/attendance")
    public ResponseEntity<byte[]> generateAttendanceReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "excel") String format,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        byte[] report = reportService.generateAttendanceReport(startDate, endDate, currentUser, format);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(getMediaType(format));
        headers.setContentDispositionFormData("attachment",
                "attendance_report_" + startDate + "_to_" + endDate + "." + format);

        return new ResponseEntity<>(report, headers, HttpStatus.OK);
    }

    @GetMapping("/leave-usage")
    public ResponseEntity<byte[]> generateLeaveUsageReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "excel") String format,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        byte[] report = reportService.generateLeaveUsageReport(startDate, endDate, currentUser, format);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(getMediaType(format));
        headers.setContentDispositionFormData("attachment",
                "leave_usage_report_" + startDate + "_to_" + endDate + "." + format);

        return new ResponseEntity<>(report, headers, HttpStatus.OK);
    }

    private MediaType getMediaType(String format) {
        if ("csv".equalsIgnoreCase(format)) {
            return MediaType.parseMediaType("text/csv");
        } else {
            return MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        }
    }
}
