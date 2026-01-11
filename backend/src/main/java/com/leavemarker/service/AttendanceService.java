package com.leavemarker.service;

import com.leavemarker.dto.attendance.AttendanceCorrectionRequest;
import com.leavemarker.dto.attendance.AttendancePunchRequest;
import com.leavemarker.dto.attendance.AttendanceResponse;
import com.leavemarker.entity.Attendance;
import com.leavemarker.entity.Employee;
import com.leavemarker.enums.AttendanceStatus;
import com.leavemarker.exception.BadRequestException;
import com.leavemarker.exception.ResourceNotFoundException;
import com.leavemarker.repository.AttendanceRepository;
import com.leavemarker.repository.EmployeeRepository;
import com.leavemarker.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public AttendanceResponse punchInOut(AttendancePunchRequest request, UserPrincipal currentUser) {
        Employee employee = employeeRepository.findByIdAndDeletedFalse(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        if (!request.getDate().equals(LocalDate.now())) {
            throw new BadRequestException("Can only punch in/out for today's date");
        }

        Optional<Attendance> existingAttendance = attendanceRepository.findByEmployeeIdAndDateAndDeletedFalse(
                employee.getId(), request.getDate());

        Attendance attendance;

        if (request.getIsPunchIn()) {
            // Punch In
            if (existingAttendance.isPresent()) {
                throw new BadRequestException("Already punched in for today");
            }

            attendance = Attendance.builder()
                    .employee(employee)
                    .date(request.getDate())
                    .punchInTime(request.getPunchTime())
                    .workType(request.getWorkType())
                    .status(AttendanceStatus.PRESENT)
                    .correctionRequested(false)
                    .correctionApproved(false)
                    .build();
        } else {
            // Punch Out
            if (!existingAttendance.isPresent()) {
                throw new BadRequestException("No punch-in record found for today");
            }

            attendance = existingAttendance.get();
            if (attendance.getPunchOutTime() != null) {
                throw new BadRequestException("Already punched out for today");
            }

            if (request.getPunchTime().isBefore(attendance.getPunchInTime())) {
                throw new BadRequestException("Punch-out time cannot be before punch-in time");
            }

            attendance.setPunchOutTime(request.getPunchTime());
        }

        attendance = attendanceRepository.save(attendance);
        return mapToResponse(attendance);
    }

    @Transactional
    public AttendanceResponse requestCorrection(Long attendanceId, AttendanceCorrectionRequest request, UserPrincipal currentUser) {
        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found"));

        // Validate that current user is the owner of this attendance
        if (!attendance.getEmployee().getId().equals(currentUser.getId())) {
            throw new BadRequestException("You can only request correction for your own attendance");
        }

        if (attendance.getCorrectionRequested() && !attendance.getCorrectionApproved()) {
            throw new BadRequestException("Correction request is already pending");
        }

        // Validate times
        if (request.getPunchInTime() != null && request.getPunchOutTime() != null) {
            if (request.getPunchOutTime().isBefore(request.getPunchInTime())) {
                throw new BadRequestException("Punch-out time cannot be before punch-in time");
            }
        }

        // Store original values in remarks for audit purposes
        String originalValues = String.format("Original - In: %s, Out: %s, Type: %s",
                attendance.getPunchInTime(),
                attendance.getPunchOutTime(),
                attendance.getWorkType());

        attendance.setCorrectionRequested(true);
        attendance.setCorrectionApproved(false);
        attendance.setRemarks(originalValues + (request.getRemarks() != null ? " | " + request.getRemarks() : ""));

        attendance = attendanceRepository.save(attendance);
        return mapToResponse(attendance);
    }

    @Transactional
    public AttendanceResponse approveCorrection(Long attendanceId, AttendanceCorrectionRequest request, UserPrincipal currentUser) {
        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found"));

        // Validate company access
        if (!attendance.getEmployee().getCompany().getId().equals(currentUser.getCompanyId())) {
            throw new BadRequestException("Access denied");
        }

        if (!attendance.getCorrectionRequested()) {
            throw new BadRequestException("No correction request found for this attendance");
        }

        if (attendance.getCorrectionApproved()) {
            throw new BadRequestException("Correction already approved");
        }

        // Apply corrections
        if (request.getPunchInTime() != null) {
            attendance.setPunchInTime(request.getPunchInTime());
        }
        if (request.getPunchOutTime() != null) {
            attendance.setPunchOutTime(request.getPunchOutTime());
        }
        if (request.getWorkType() != null) {
            attendance.setWorkType(request.getWorkType());
        }

        attendance.setCorrectionApproved(true);
        attendance = attendanceRepository.save(attendance);
        return mapToResponse(attendance);
    }

    @Transactional
    public AttendanceResponse rejectCorrection(Long attendanceId, String reason, UserPrincipal currentUser) {
        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found"));

        // Validate company access
        if (!attendance.getEmployee().getCompany().getId().equals(currentUser.getCompanyId())) {
            throw new BadRequestException("Access denied");
        }

        if (!attendance.getCorrectionRequested()) {
            throw new BadRequestException("No correction request found for this attendance");
        }

        attendance.setCorrectionRequested(false);
        attendance.setCorrectionApproved(false);
        if (reason != null) {
            attendance.setRemarks(attendance.getRemarks() + " | Rejection Reason: " + reason);
        }

        attendance = attendanceRepository.save(attendance);
        return mapToResponse(attendance);
    }

    public AttendanceResponse getAttendance(Long id, UserPrincipal currentUser) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found"));

        // Validate access
        if (!attendance.getEmployee().getId().equals(currentUser.getId()) &&
            !attendance.getEmployee().getCompany().getId().equals(currentUser.getCompanyId())) {
            throw new BadRequestException("Access denied");
        }

        return mapToResponse(attendance);
    }

    public AttendanceResponse getTodayAttendance(UserPrincipal currentUser) {
        Optional<Attendance> attendance = attendanceRepository.findByEmployeeIdAndDateAndDeletedFalse(
                currentUser.getId(), LocalDate.now());

        return attendance.map(this::mapToResponse).orElse(null);
    }

    public List<AttendanceResponse> getMyAttendance(UserPrincipal currentUser) {
        List<Attendance> attendances = attendanceRepository.findByEmployeeIdAndDeletedFalse(
                currentUser.getId());
        return attendances.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<AttendanceResponse> getMyAttendanceByDateRange(
            LocalDate startDate, LocalDate endDate, UserPrincipal currentUser) {
        List<Attendance> attendances = attendanceRepository.findByEmployeeIdAndDateRange(
                currentUser.getId(), startDate, endDate);
        return attendances.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<AttendanceResponse> getAttendanceByDateRange(
            LocalDate startDate, LocalDate endDate, UserPrincipal currentUser) {
        List<Attendance> attendances = attendanceRepository.findByCompanyIdAndDateRange(
                currentUser.getCompanyId(), startDate, endDate);
        return attendances.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<AttendanceResponse> getPendingCorrections(UserPrincipal currentUser) {
        List<Attendance> attendances = attendanceRepository.findPendingCorrections(
                currentUser.getCompanyId());
        return attendances.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AttendanceResponse markAttendance(Long employeeId, LocalDate date, AttendanceStatus status,
                                            String remarks, UserPrincipal currentUser) {
        Employee employee = employeeRepository.findByIdAndDeletedFalse(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        // Validate company access
        if (!employee.getCompany().getId().equals(currentUser.getCompanyId())) {
            throw new BadRequestException("Access denied");
        }

        Optional<Attendance> existingAttendance = attendanceRepository.findByEmployeeIdAndDateAndDeletedFalse(
                employeeId, date);

        Attendance attendance;
        if (existingAttendance.isPresent()) {
            attendance = existingAttendance.get();
            attendance.setStatus(status);
            if (remarks != null) {
                attendance.setRemarks(remarks);
            }
        } else {
            attendance = Attendance.builder()
                    .employee(employee)
                    .date(date)
                    .status(status)
                    .remarks(remarks)
                    .correctionRequested(false)
                    .correctionApproved(false)
                    .build();
        }

        attendance = attendanceRepository.save(attendance);
        return mapToResponse(attendance);
    }

    private AttendanceResponse mapToResponse(Attendance attendance) {
        return AttendanceResponse.builder()
                .id(attendance.getId())
                .employeeId(attendance.getEmployee().getId())
                .employeeName(attendance.getEmployee().getFullName())
                .employeeEmail(attendance.getEmployee().getEmail())
                .date(attendance.getDate())
                .punchInTime(attendance.getPunchInTime())
                .punchOutTime(attendance.getPunchOutTime())
                .workType(attendance.getWorkType())
                .status(attendance.getStatus())
                .remarks(attendance.getRemarks())
                .correctionRequested(attendance.getCorrectionRequested())
                .correctionApproved(attendance.getCorrectionApproved())
                .build();
    }
}
