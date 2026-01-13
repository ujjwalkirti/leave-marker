package com.leavemarker.dto.subscription;

import com.leavemarker.dto.plan.PlanResponse;
import com.leavemarker.enums.BillingCycle;
import com.leavemarker.enums.SubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionResponse {
    private Long id;
    private Long companyId;
    private PlanResponse plan;
    private SubscriptionStatus status;
    private BillingCycle billingCycle;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime currentPeriodStart;
    private LocalDateTime currentPeriodEnd;
    private BigDecimal amount;
    private Boolean autoRenew;
    private Boolean isPaid;
    private String notes;
}
