package com.leavemarker.dto.plan;

import com.leavemarker.enums.BillingCycle;
import com.leavemarker.enums.PlanTier;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanRequest {
    @NotBlank(message = "Plan name is required")
    private String name;

    private String description;

    @NotNull(message = "Plan tier is required")
    private PlanTier tier;

    @NotNull(message = "Billing cycle is required")
    private BillingCycle billingCycle;

    @NotNull(message = "Monthly price is required")
    @PositiveOrZero(message = "Monthly price must be zero or positive")
    private BigDecimal monthlyPrice;

    @NotNull(message = "Yearly price is required")
    @PositiveOrZero(message = "Yearly price must be zero or positive")
    private BigDecimal yearlyPrice;

    @PositiveOrZero(message = "Min employees must be zero or positive")
    private Integer minEmployees;

    @PositiveOrZero(message = "Max employees must be zero or positive")
    private Integer maxEmployees;

    @PositiveOrZero(message = "Max leave policies must be zero or positive")
    private Integer maxLeavePolicies;

    private Boolean active;
    private Boolean attendanceTracking;
    private Boolean advancedReports;
    private Boolean customLeaveTypes;
    private Boolean apiAccess;
    private Boolean prioritySupport;
    private Boolean attendanceRateAnalytics;
}
