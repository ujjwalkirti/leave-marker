package com.leavemarker.controller;

import com.leavemarker.dto.ApiResponse;
import com.leavemarker.dto.holiday.HolidayRequest;
import com.leavemarker.dto.holiday.HolidayResponse;
import com.leavemarker.security.UserPrincipal;
import com.leavemarker.service.HolidayService;
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
import java.util.List;

@RestController
@RequestMapping("/holidays")
@RequiredArgsConstructor
public class HolidayController {

    private final HolidayService holidayService;
    private final PlanValidationService planValidationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HR_ADMIN')")
    public ResponseEntity<ApiResponse<HolidayResponse>> createHoliday(
            @Valid @RequestBody HolidayRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        // Validate holiday limit based on plan
        planValidationService.validateHolidayLimit(currentUser.getCompanyId());

        HolidayResponse response = holidayService.createHoliday(request, currentUser);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Holiday created successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<HolidayResponse>> getHoliday(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        HolidayResponse response = holidayService.getHoliday(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Holiday retrieved successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<HolidayResponse>>> getAllHolidays(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<HolidayResponse> response = holidayService.getAllHolidays(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Holidays retrieved successfully", response));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<HolidayResponse>>> getActiveHolidays(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<HolidayResponse> response = holidayService.getActiveHolidays(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Active holidays retrieved successfully", response));
    }

    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<List<HolidayResponse>>> getHolidaysByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<HolidayResponse> response = holidayService.getHolidaysByDateRange(startDate, endDate, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Holidays retrieved successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HR_ADMIN')")
    public ResponseEntity<ApiResponse<HolidayResponse>> updateHoliday(
            @PathVariable Long id,
            @Valid @RequestBody HolidayRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        HolidayResponse response = holidayService.updateHoliday(id, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Holiday updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HR_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteHoliday(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        holidayService.deleteHoliday(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Holiday deleted successfully"));
    }
}
