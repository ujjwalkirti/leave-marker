package com.leavemarker.dto.payment;

import com.leavemarker.enums.BillingCycle;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInitiateRequest {
    @NotNull(message = "Plan ID is required")
    private Long planId;

    @NotNull(message = "Billing cycle is required")
    private BillingCycle billingCycle;
}
