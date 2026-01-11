package com.leavemarker.entity;

import com.leavemarker.enums.LeaveStatus;
import com.leavemarker.enums.LeaveType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "leave_applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveApplication extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LeaveType leaveType;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private Double numberOfDays;

    @Column(nullable = false)
    private Boolean isHalfDay = false;

    @Column(length = 1000)
    private String reason;

    @Column(length = 500)
    private String attachmentUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LeaveStatus status = LeaveStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_manager_id")
    private Employee approvedByManager;

    @Column
    private LocalDate managerApprovalDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_hr_id")
    private Employee approvedByHr;

    @Column
    private LocalDate hrApprovalDate;

    @Column(length = 500)
    private String rejectionReason;

    @Column
    private LocalDate rejectionDate;

    @Column(nullable = false)
    private Boolean requiresHrApproval = false;
}
