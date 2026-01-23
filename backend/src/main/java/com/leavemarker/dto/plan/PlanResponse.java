package com.leavemarker.dto.plan;

import com.leavemarker.enums.BillingCycle;
import com.leavemarker.enums.PlanTier;
import com.leavemarker.enums.PlanType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanResponse {
    private Long id;
    private String name;
    private String description;
    private PlanTier tier;
    private PlanType planType;
    private BillingCycle billingCycle;
    private BigDecimal monthlyPrice;
    private BigDecimal yearlyPrice;
    private Integer minEmployees;
    private Integer maxEmployees;
    private Integer maxLeavePolicies;
    private Integer maxHolidays;
    private Boolean active;

    // Feature flags
    private Boolean attendanceManagement;
    private Boolean reportsDownload;
    private Boolean multipleLeavePolicies;
    private Boolean unlimitedHolidays;
    private Boolean attendanceRateAnalytics;
    private Boolean advancedReports;
    private Boolean customLeaveTypes;
    private Boolean apiAccess;
    private Boolean prioritySupport;

    // Pricing per employee
    private BigDecimal pricePerEmployee;

    // Report download pricing (add-on for MID_TIER)
    private BigDecimal reportDownloadPriceUnder50;
    private BigDecimal reportDownloadPrice50Plus;
}
