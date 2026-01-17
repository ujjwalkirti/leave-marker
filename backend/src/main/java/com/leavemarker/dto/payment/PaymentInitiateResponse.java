package com.leavemarker.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInitiateResponse {
    private String razorpayOrderId;
    private String razorpayKeyId;
    private Long amount; // Amount in paise
    private String currency;
    private String transactionId;
    private String companyName;
    private String companyEmail;
    private Long employeeCount;
    private Long pricePerEmployee; // Price per employee in paise
}
