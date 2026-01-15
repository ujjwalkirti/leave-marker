package com.leavemarker.entity;

import com.leavemarker.enums.BillingCycle;
import com.leavemarker.enums.PaymentStatus;
import com.leavemarker.enums.PaymentType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_payment_transaction_id", columnList = "transactionId"),
    @Index(name = "idx_payment_razorpay_order_id", columnList = "razorpayOrderId"),
    @Index(name = "idx_payment_razorpay_payment_id", columnList = "razorpayPaymentId"),
    @Index(name = "idx_payment_company", columnList = "company_id"),
    @Index(name = "idx_payment_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    // Unique internal transaction ID
    @Column(nullable = false, unique = true, length = 100)
    private String transactionId;

    // Razorpay Order ID (created before payment)
    @Column(length = 100)
    private String razorpayOrderId;

    // Razorpay Payment ID (after successful payment)
    @Column(length = 100)
    private String razorpayPaymentId;

    // Razorpay Signature (for verification)
    @Column(length = 256)
    private String razorpaySignature;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentType paymentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BillingCycle billingCycle;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(length = 10)
    @Builder.Default
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(length = 100)
    private String paymentMethod;

    // Timestamps for tracking
    private LocalDateTime initiatedAt;

    private LocalDateTime paidAt;

    private LocalDateTime failedAt;

    private LocalDateTime refundedAt;

    // Period this payment covers
    @Column(nullable = false)
    private LocalDateTime periodStart;

    @Column(nullable = false)
    private LocalDateTime periodEnd;

    // Webhook tracking
    @Column(length = 50)
    private String webhookStatus;

    private LocalDateTime webhookReceivedAt;

    @Column(columnDefinition = "TEXT")
    private String webhookPayload;

    // Audit info
    @Column(length = 50)
    private String ipAddress;

    @Column(length = 500)
    private String userAgent;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    @Column(length = 500)
    private String failureReason;

    @Column(length = 500)
    private String refundReason;

    // Idempotency key for retries
    @Column(length = 100, unique = true)
    private String idempotencyKey;

    // Retry tracking
    @Column(nullable = false)
    @Builder.Default
    private Integer retryCount = 0;

    private LocalDateTime lastRetryAt;
}
