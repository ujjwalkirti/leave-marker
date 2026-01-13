package com.leavemarker.entity;

import com.leavemarker.enums.BillingCycle;
import com.leavemarker.enums.PlanTier;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plan extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanTier tier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BillingCycle billingCycle;

    // Pricing
    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal monthlyPrice = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal yearlyPrice = BigDecimal.ZERO;

    // Employee limits
    @Column(nullable = false)
    @Builder.Default
    private Integer minEmployees = 1;

    @Column(nullable = false)
    @Builder.Default
    private Integer maxEmployees = 10;

    // Leave policy limits
    @Column(nullable = false)
    @Builder.Default
    private Integer maxLeavePolicies = 3;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    // Feature flags
    @Column(nullable = false)
    @Builder.Default
    private Boolean attendanceTracking = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean advancedReports = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean customLeaveTypes = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean apiAccess = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean prioritySupport = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean attendanceRateAnalytics = false;
}
