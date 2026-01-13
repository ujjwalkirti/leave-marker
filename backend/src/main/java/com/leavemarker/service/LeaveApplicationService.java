package com.leavemarker.service;

import com.leavemarker.dto.leaveapplication.LeaveApplicationRequest;
import com.leavemarker.dto.leaveapplication.LeaveApplicationResponse;
import com.leavemarker.dto.leaveapplication.LeaveApprovalRequest;
import com.leavemarker.entity.Employee;
import com.leavemarker.entity.LeaveApplication;
import com.leavemarker.entity.LeaveBalance;
import com.leavemarker.entity.LeavePolicy;
import com.leavemarker.enums.LeaveStatus;
import com.leavemarker.exception.BadRequestException;
import com.leavemarker.exception.ResourceNotFoundException;
import com.leavemarker.repository.EmployeeRepository;
import com.leavemarker.repository.LeaveApplicationRepository;
import com.leavemarker.repository.LeaveBalanceRepository;
import com.leavemarker.repository.LeavePolicyRepository;
import com.leavemarker.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveApplicationService {

    private final LeaveApplicationRepository leaveApplicationRepository;
    private final EmployeeRepository employeeRepository;
    private final LeavePolicyRepository leavePolicyRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;

    @Transactional
    public LeaveApplicationResponse applyLeave(LeaveApplicationRequest request, UserPrincipal currentUser) {
        Employee employee = employeeRepository.findByIdAndDeletedFalse(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        // Validate dates
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("End date cannot be before start date");
        }

        if (request.getStartDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Cannot apply leave for past dates");
        }

        // Validate half-day
        if (request.getIsHalfDay() && !request.getStartDate().equals(request.getEndDate())) {
            throw new BadRequestException("Half day leave can only be for a single day");
        }

        // Check leave policy
        LeavePolicy policy = leavePolicyRepository.findByCompanyIdAndLeaveTypeAndDeletedFalse(
                        currentUser.getCompanyId(), request.getLeaveType())
                .orElseThrow(() -> new ResourceNotFoundException("Leave policy not found for this leave type"));

        if (!policy.getActive()) {
            throw new BadRequestException("This leave type is not active");
        }

        if (request.getIsHalfDay() && !policy.getHalfDayAllowed()) {
            throw new BadRequestException("Half day leave is not allowed for this leave type");
        }

        // Calculate number of days
        double numberOfDays = calculateLeaveDays(request.getStartDate(), request.getEndDate(), request.getIsHalfDay());

        // Check for overlapping leaves
        List<LeaveApplication> overlappingLeaves = leaveApplicationRepository.findOverlappingLeaves(
                employee.getId(), request.getStartDate(), request.getEndDate());
        if (!overlappingLeaves.isEmpty()) {
            throw new BadRequestException("Leave dates overlap with existing leave application");
        }

        // Check leave balance
        int currentYear = LocalDate.now().getYear();
        LeaveBalance balance = leaveBalanceRepository.findByEmployeeIdAndLeaveTypeAndYearAndDeletedFalse(
                employee.getId(), request.getLeaveType(), currentYear).orElse(null);

        if (balance != null && balance.getAvailable() < numberOfDays) {
            throw new BadRequestException("Insufficient leave balance. Available: " + balance.getAvailable() + " days");
        }

        // Determine if HR approval is required (e.g., leaves > 5 days)
        boolean requiresHrApproval = numberOfDays > 5;

        // Create leave application
        LeaveApplication application = LeaveApplication.builder()
                .employee(employee)
                .leaveType(request.getLeaveType())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .numberOfDays(numberOfDays)
                .isHalfDay(request.getIsHalfDay())
                .reason(request.getReason())
                .attachmentUrl(request.getAttachmentUrl())
                .status(LeaveStatus.PENDING)
                .requiresHrApproval(requiresHrApproval)
                .build();

        application = leaveApplicationRepository.save(application);

        // Update leave balance - mark as pending
        if (balance != null) {
            balance.setPending(balance.getPending() + numberOfDays);
            balance.setAvailable(balance.getTotalQuota() - balance.getUsed() - balance.getPending());
            leaveBalanceRepository.save(balance);
        }

        return mapToResponse(application);
    }

    @Transactional
    public LeaveApplicationResponse approveByManager(Long applicationId, LeaveApprovalRequest request, UserPrincipal currentUser) {
        LeaveApplication application = leaveApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave application not found"));

        // Validate that current user is the manager of the employee
        Employee manager = employeeRepository.findByIdAndDeletedFalse(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));

        if (application.getEmployee().getManager() == null ||
            !application.getEmployee().getManager().getId().equals(manager.getId())) {
            throw new BadRequestException("You are not authorized to approve this leave");
        }

        if (application.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("Leave application is not in pending status");
        }

        if (request.getApproved()) {
            // Manager approved
            application.setApprovedByManager(manager);
            application.setManagerApprovalDate(LocalDate.now());

            if (application.getRequiresHrApproval()) {
                // Keep status as PENDING, waiting for HR approval
                application.setStatus(LeaveStatus.PENDING);
            } else {
                // No HR approval needed, mark as approved
                application.setStatus(LeaveStatus.APPROVED);
                updateLeaveBalanceOnApproval(application);
            }
        } else {
            // Manager rejected
            application.setStatus(LeaveStatus.REJECTED);
            application.setRejectionReason(request.getReason());
            application.setRejectionDate(LocalDate.now());
            revertPendingBalance(application);
        }

        application = leaveApplicationRepository.save(application);
        return mapToResponse(application);
    }

    @Transactional
    public LeaveApplicationResponse approveByHr(Long applicationId, LeaveApprovalRequest request, UserPrincipal currentUser) {
        LeaveApplication application = leaveApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave application not found"));

        // Validate company access
        if (!application.getEmployee().getCompany().getId().equals(currentUser.getCompanyId())) {
            throw new BadRequestException("Access denied");
        }

        if (application.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("Leave application is not in pending status");
        }

        if (!application.getRequiresHrApproval()) {
            throw new BadRequestException("This leave application does not require HR approval");
        }

        if (application.getApprovedByManager() == null) {
            throw new BadRequestException("Leave must be approved by manager first");
        }

        Employee hr = employeeRepository.findByIdAndDeletedFalse(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("HR not found"));

        if (request.getApproved()) {
            // HR approved
            application.setApprovedByHr(hr);
            application.setHrApprovalDate(LocalDate.now());
            application.setStatus(LeaveStatus.APPROVED);
            updateLeaveBalanceOnApproval(application);
        } else {
            // HR rejected
            application.setStatus(LeaveStatus.REJECTED);
            application.setRejectionReason(request.getReason());
            application.setRejectionDate(LocalDate.now());
            revertPendingBalance(application);
        }

        application = leaveApplicationRepository.save(application);
        return mapToResponse(application);
    }

    @Transactional
    public LeaveApplicationResponse cancelLeave(Long applicationId, UserPrincipal currentUser) {
        LeaveApplication application = leaveApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave application not found"));

        // Validate that current user is the employee who applied
        if (!application.getEmployee().getId().equals(currentUser.getId())) {
            throw new BadRequestException("You can only cancel your own leave applications");
        }

        if (application.getStatus() == LeaveStatus.CANCELLED || application.getStatus() == LeaveStatus.REJECTED) {
            throw new BadRequestException("Leave application is already " + application.getStatus());
        }

        // If leave is approved and start date has passed, don't allow cancellation
        if (application.getStatus() == LeaveStatus.APPROVED && application.getStartDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Cannot cancel leave that has already started");
        }

        LeaveStatus previousStatus = application.getStatus();
        application.setStatus(LeaveStatus.CANCELLED);
        application = leaveApplicationRepository.save(application);

        // Revert leave balance
        if (previousStatus == LeaveStatus.APPROVED) {
            revertApprovedBalance(application);
        } else if (previousStatus == LeaveStatus.PENDING) {
            revertPendingBalance(application);
        }

        return mapToResponse(application);
    }

    public LeaveApplicationResponse getLeaveApplication(Long id, UserPrincipal currentUser) {
        LeaveApplication application = leaveApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave application not found"));

        // Validate access
        if (!application.getEmployee().getId().equals(currentUser.getId()) &&
            !application.getEmployee().getCompany().getId().equals(currentUser.getCompanyId())) {
            throw new BadRequestException("Access denied");
        }

        return mapToResponse(application);
    }

    public List<LeaveApplicationResponse> getMyLeaveApplications(UserPrincipal currentUser) {
        List<LeaveApplication> applications = leaveApplicationRepository.findByEmployeeIdAndDeletedFalse(
                currentUser.getId());
        return applications.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<LeaveApplicationResponse> getPendingApprovalsForManager(UserPrincipal currentUser) {
        List<LeaveApplication> applications = leaveApplicationRepository.findByManagerIdAndStatus(
                currentUser.getId(), LeaveStatus.PENDING);
        // Filter out those that already have manager approval but are waiting for HR
        return applications.stream()
                .filter(app -> app.getApprovedByManager() == null)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<LeaveApplicationResponse> getPendingApprovalsForHr(UserPrincipal currentUser) {
        List<LeaveApplication> applications = leaveApplicationRepository.findByCompanyIdAndStatus(
                currentUser.getCompanyId(), LeaveStatus.PENDING);
        // Filter to only those requiring HR approval and already approved by manager
        return applications.stream()
                .filter(app -> app.getRequiresHrApproval() && app.getApprovedByManager() != null && app.getApprovedByHr() == null)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<LeaveApplicationResponse> getLeaveApplicationsByDateRange(
            LocalDate startDate, LocalDate endDate, UserPrincipal currentUser) {
        List<LeaveApplication> applications = leaveApplicationRepository.findByCompanyIdAndDateRange(
                currentUser.getCompanyId(), startDate, endDate);
        return applications.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private double calculateLeaveDays(LocalDate startDate, LocalDate endDate, boolean isHalfDay) {
        if (isHalfDay) {
            return 0.5;
        }

        long totalDays = 0;
        LocalDate date = startDate;
        while (!date.isAfter(endDate)) {
            // Skip weekends (Saturday and Sunday)
            if (date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY) {
                totalDays++;
            }
            date = date.plusDays(1);
        }

        return (double) totalDays;
    }

    private void updateLeaveBalanceOnApproval(LeaveApplication application) {
        int currentYear = LocalDate.now().getYear();
        LeaveBalance balance = leaveBalanceRepository.findByEmployeeIdAndLeaveTypeAndYearAndDeletedFalse(
                application.getEmployee().getId(), application.getLeaveType(), currentYear).orElse(null);

        if (balance != null) {
            balance.setPending(balance.getPending() - application.getNumberOfDays());
            balance.setUsed(balance.getUsed() + application.getNumberOfDays());
            balance.setAvailable(balance.getTotalQuota() - balance.getUsed() - balance.getPending());
            leaveBalanceRepository.save(balance);
        }
    }

    private void revertPendingBalance(LeaveApplication application) {
        int currentYear = LocalDate.now().getYear();
        LeaveBalance balance = leaveBalanceRepository.findByEmployeeIdAndLeaveTypeAndYearAndDeletedFalse(
                application.getEmployee().getId(), application.getLeaveType(), currentYear).orElse(null);

        if (balance != null) {
            balance.setPending(balance.getPending() - application.getNumberOfDays());
            balance.setAvailable(balance.getTotalQuota() - balance.getUsed() - balance.getPending());
            leaveBalanceRepository.save(balance);
        }
    }

    private void revertApprovedBalance(LeaveApplication application) {
        int currentYear = LocalDate.now().getYear();
        LeaveBalance balance = leaveBalanceRepository.findByEmployeeIdAndLeaveTypeAndYearAndDeletedFalse(
                application.getEmployee().getId(), application.getLeaveType(), currentYear).orElse(null);

        if (balance != null) {
            balance.setUsed(balance.getUsed() - application.getNumberOfDays());
            balance.setAvailable(balance.getTotalQuota() - balance.getUsed() - balance.getPending());
            leaveBalanceRepository.save(balance);
        }
    }

    private LeaveApplicationResponse mapToResponse(LeaveApplication application) {
        return LeaveApplicationResponse.builder()
                .id(application.getId())
                .employeeId(application.getEmployee().getId())
                .employeeName(application.getEmployee().getFullName())
                .employeeEmail(application.getEmployee().getEmail())
                .leaveType(application.getLeaveType())
                .startDate(application.getStartDate())
                .endDate(application.getEndDate())
                .numberOfDays(application.getNumberOfDays())
                .isHalfDay(application.getIsHalfDay())
                .reason(application.getReason())
                .attachmentUrl(application.getAttachmentUrl())
                .status(application.getStatus())
                .approvedByManagerId(application.getApprovedByManager() != null ? application.getApprovedByManager().getId() : null)
                .approvedByManagerName(application.getApprovedByManager() != null ? application.getApprovedByManager().getFullName() : null)
                .managerApprovalDate(application.getManagerApprovalDate())
                .approvedByHrId(application.getApprovedByHr() != null ? application.getApprovedByHr().getId() : null)
                .approvedByHrName(application.getApprovedByHr() != null ? application.getApprovedByHr().getFullName() : null)
                .hrApprovalDate(application.getHrApprovalDate())
                .rejectionReason(application.getRejectionReason())
                .rejectionDate(application.getRejectionDate())
                .requiresHrApproval(application.getRequiresHrApproval())
                .build();
    }
}
