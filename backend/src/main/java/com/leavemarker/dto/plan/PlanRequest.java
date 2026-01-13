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

    @PositiveOrZero(message = "Max holidays must be zero or positive")
    private Integer maxHolidays;

    private Boolean active;

    // Feature flags
    private Boolean attendanceManagement;
    private Boolean reportsDownload;
    private Boolean multipleLeavePolicies;
    private Boolean unlimitedHolidays;
    private Boolean attendanceRateAnalytics;

    // Report download pricing (add-on for MID_TIER)
    @PositiveOrZero(message = "Report download price must be zero or positive")
    private BigDecimal reportDownloadPriceUnder50;

    @PositiveOrZero(message = "Report download price must be zero or positive")
    private BigDecimal reportDownloadPrice50Plus;
}
