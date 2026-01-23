package com.leavemarker.entity;

import com.leavemarker.enums.BillingCycle;
import com.leavemarker.enums.PlanTier;
import com.leavemarker.enums.PlanType;
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
    @Builder.Default
    private PlanType planType = PlanType.FREE;

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
    private Integer maxLeavePolicies = 1; // FREE: 1, MID_TIER: unlimited (-1)

    // Holiday limits
    @Column(nullable = false)
    @Builder.Default
    private Integer maxHolidays = 6; // FREE: 6, MID_TIER: unlimited (-1)

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    // Feature flags
    @Column(nullable = false)
    @Builder.Default
    private Boolean attendanceManagement = false; // MID_TIER only

    @Column(nullable = false)
    @Builder.Default
    private Boolean reportsDownload = false; // MID_TIER only

    @Column(nullable = false)
    @Builder.Default
    private Boolean multipleLeavePolicies = false; // MID_TIER only

    @Column(nullable = false)
    @Builder.Default
    private Boolean unlimitedHolidays = false; // MID_TIER only

    @Column(nullable = false)
    @Builder.Default
    private Boolean attendanceRateAnalytics = false; // MID_TIER only

    @Column(nullable = false)
    @Builder.Default
    private Boolean advancedReports = false; // MID_TIER only

    @Column(nullable = false)
    @Builder.Default
    private Boolean customLeaveTypes = false; // MID_TIER only

    @Column(nullable = false)
    @Builder.Default
    private Boolean apiAccess = false; // MID_TIER only

    @Column(nullable = false)
    @Builder.Default
    private Boolean prioritySupport = false; // MID_TIER only

    // Pricing per employee
    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal pricePerEmployee = BigDecimal.ZERO;

    // Report download pricing (add-on for MID_TIER)
    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal reportDownloadPriceUnder50 = BigDecimal.ZERO; // ₹200/month for <50 employees

    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal reportDownloadPrice50Plus = BigDecimal.ZERO; // ₹400/month for >=50 employees
}
