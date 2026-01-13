package com.leavemarker.dto.plan;

import com.leavemarker.enums.BillingCycle;
import com.leavemarker.enums.PlanTier;
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
    private BillingCycle billingCycle;
    private BigDecimal monthlyPrice;
    private BigDecimal yearlyPrice;
    private Integer minEmployees;
    private Integer maxEmployees;
    private Integer maxLeavePolicies;
    private Boolean active;
    private Boolean attendanceTracking;
    private Boolean advancedReports;
    private Boolean customLeaveTypes;
    private Boolean apiAccess;
    private Boolean prioritySupport;
    private Boolean attendanceRateAnalytics;
}
