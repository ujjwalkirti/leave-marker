package com.leavemarker.entity;

import com.leavemarker.enums.LeaveType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "leave_balances",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"employee_id", "leave_type", "year"})
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveBalance extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type", nullable = false, length = 30)
    private LeaveType leaveType;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private Double totalQuota = 0.0;

    @Column(nullable = false)
    private Double used = 0.0;

    @Column(nullable = false)
    private Double pending = 0.0;

    @Column(nullable = false)
    private Double available = 0.0;

    @Column(nullable = false)
    private Double carriedForward = 0.0;
}
