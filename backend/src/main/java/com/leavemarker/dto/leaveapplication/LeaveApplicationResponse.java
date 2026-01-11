package com.leavemarker.dto.leaveapplication;

import com.leavemarker.enums.LeaveStatus;
import com.leavemarker.enums.LeaveType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LeaveApplicationResponse {

    private Long id;
    private Long employeeId;
    private String employeeName;
    private String employeeEmail;
    private LeaveType leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double numberOfDays;
    private Boolean isHalfDay;
    private String reason;
    private String attachmentUrl;
    private LeaveStatus status;
    private Long approvedByManagerId;
    private String approvedByManagerName;
    private LocalDate managerApprovalDate;
    private Long approvedByHrId;
    private String approvedByHrName;
    private LocalDate hrApprovalDate;
    private String rejectionReason;
    private LocalDate rejectionDate;
    private Boolean requiresHrApproval;
}
