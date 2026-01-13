package com.leavemarker.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentWebhookRequest {
    private String dodoPaymentId;
    private String status;
    private String paymentMethod;
    private String metadata;
}
