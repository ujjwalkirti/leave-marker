package com.leavemarker.dto.subscription;

import com.leavemarker.enums.PlanTier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionFeatureResponse {
    private boolean hasActiveSubscription;
    private Long subscriptionId;
    private boolean isPaid;
    private boolean isValid;
    private PlanTier tier;
    private String planName;

    // Limits
    private int maxEmployees;
    private int currentEmployees;
    private int remainingEmployeeSlots;
    private int maxLeavePolicies;
    private int currentLeavePolicies;
    private int remainingLeavePolicySlots;

    // Feature flags
    private int maxHolidays;
    private boolean attendanceManagement;
    private boolean reportsDownload;
    private boolean multipleLeavePolicies;
    private boolean unlimitedHolidays;
    private boolean attendanceRateAnalytics;

    // Period info
    private LocalDateTime currentPeriodEnd;
}
