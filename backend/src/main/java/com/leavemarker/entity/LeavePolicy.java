package com.leavemarker.entity;

import com.leavemarker.enums.LeaveType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "leave_policies",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"company_id", "leave_type"})
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeavePolicy extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type", nullable = false, length = 30)
    private LeaveType leaveType;

    @Column(nullable = false)
    private Integer annualQuota;

    @Column(nullable = false)
    @Builder.Default
    private Double monthlyAccrual = 0.0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean carryForward = false;

    @Column(nullable = false)
    @Builder.Default
    private Integer maxCarryForward = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean encashmentAllowed = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean halfDayAllowed = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}
