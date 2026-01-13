package com.leavemarker.service;

import com.leavemarker.config.DodoPaymentConfig;
import com.leavemarker.dto.payment.*;
import com.leavemarker.entity.Company;
import com.leavemarker.entity.Payment;
import com.leavemarker.entity.Subscription;
import com.leavemarker.enums.BillingCycle;
import com.leavemarker.enums.PaymentStatus;
import com.leavemarker.enums.PaymentType;
import com.leavemarker.enums.SubscriptionStatus;
import com.leavemarker.exception.BadRequestException;
import com.leavemarker.exception.ResourceNotFoundException;
import com.leavemarker.repository.CompanyRepository;
import com.leavemarker.repository.PaymentRepository;
import com.leavemarker.repository.SubscriptionRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final CompanyRepository companyRepository;
    private final DodoPaymentConfig dodoPaymentConfig;
    private final RestTemplate restTemplate = new RestTemplate();

    @Transactional(readOnly = true)
    public List<PaymentResponse> getCompanyPayments(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));

        return paymentRepository.findByCompany(company).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));
        return convertToResponse(payment);
    }

    @Transactional
    public PaymentInitiateResponse initiatePayment(Long companyId, PaymentInitiateRequest request, HttpServletRequest httpRequest) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));

        Subscription subscription = subscriptionRepository.findById(request.getSubscriptionId())
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + request.getSubscriptionId()));

        if (!subscription.getCompany().getId().equals(companyId)) {
            throw new BadRequestException("Subscription does not belong to this company");
        }

        // Generate unique IDs
        String transactionId = "TXN-" + UUID.randomUUID().toString();
        String idempotencyKey = "IDEM-" + UUID.randomUUID().toString();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime periodEnd = subscription.getBillingCycle() == BillingCycle.YEARLY
                ? now.plusYears(1)
                : now.plusMonths(1);

        // Create payment record with full tracking
        Payment payment = Payment.builder()
                .subscription(subscription)
                .company(company)
                .transactionId(transactionId)
                .paymentType(PaymentType.NEW_SUBSCRIPTION)
                .billingCycle(subscription.getBillingCycle())
                .amount(request.getAmount())
                .taxAmount(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(request.getAmount())
                .currency("INR")
                .status(PaymentStatus.PENDING)
                .initiatedAt(now)
                .periodStart(subscription.getCurrentPeriodStart())
                .periodEnd(periodEnd)
                .idempotencyKey(idempotencyKey)
                .ipAddress(getClientIp(httpRequest))
                .userAgent(httpRequest != null ? httpRequest.getHeader("User-Agent") : null)
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        try {
            // Call Dodo Payments API
            String dodoPaymentId = createDodoPayment(savedPayment);
            savedPayment.setDodoPaymentId(dodoPaymentId);
            paymentRepository.save(savedPayment);

            String paymentUrl = dodoPaymentConfig.getBaseUrl() + "/checkout/" + dodoPaymentId;

            log.info("Payment initiated for company: {}, transaction: {}, idempotency: {}",
                    companyId, transactionId, idempotencyKey);

            return PaymentInitiateResponse.builder()
                    .paymentUrl(paymentUrl)
                    .transactionId(transactionId)
                    .dodoPaymentId(dodoPaymentId)
                    .build();

        } catch (Exception e) {
            savedPayment.setStatus(PaymentStatus.FAILED);
            savedPayment.setFailedAt(now);
            savedPayment.setFailureReason("Failed to create payment: " + e.getMessage());
            paymentRepository.save(savedPayment);
            log.error("Failed to initiate payment for transaction: {}", transactionId, e);
            throw new BadRequestException("Failed to initiate payment: " + e.getMessage());
        }
    }

    @Transactional
    public void handleWebhook(PaymentWebhookRequest webhookRequest, String rawPayload) {
        Payment payment = paymentRepository.findByDodoPaymentId(webhookRequest.getDodoPaymentId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with dodoPaymentId: " + webhookRequest.getDodoPaymentId()));

        LocalDateTime now = LocalDateTime.now();

        // Track webhook receipt
        payment.setWebhookStatus(webhookRequest.getStatus());
        payment.setWebhookReceivedAt(now);
        payment.setWebhookPayload(rawPayload);

        String status = webhookRequest.getStatus();

        switch (status.toUpperCase()) {
            case "SUCCESS", "COMPLETED" -> {
                payment.setStatus(PaymentStatus.SUCCESS);
                payment.setPaidAt(now);
                payment.setPaymentMethod(webhookRequest.getPaymentMethod());

                // Update subscription
                Subscription subscription = payment.getSubscription();
                subscription.setIsPaid(true);
                subscription.setCurrentPeriodEnd(payment.getPeriodEnd());
                subscription.setEndDate(payment.getPeriodEnd());
                subscription.setStatus(SubscriptionStatus.ACTIVE);
                subscriptionRepository.save(subscription);

                log.info("Payment successful for transaction: {}, subscription marked as paid", payment.getTransactionId());
            }
            case "FAILED" -> {
                payment.setStatus(PaymentStatus.FAILED);
                payment.setFailedAt(now);
                payment.setFailureReason("Payment failed via webhook");
                log.warn("Payment failed for transaction: {}", payment.getTransactionId());
            }
            case "REFUNDED" -> {
                payment.setStatus(PaymentStatus.REFUNDED);
                payment.setRefundedAt(now);
                log.info("Payment refunded for transaction: {}", payment.getTransactionId());
            }
            default -> log.warn("Unknown payment status received: {}", status);
        }

        payment.setMetadata(webhookRequest.getMetadata());
        paymentRepository.save(payment);
    }

    @Transactional
    public void retryPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        if (payment.getStatus() != PaymentStatus.FAILED) {
            throw new BadRequestException("Can only retry failed payments");
        }

        if (payment.getRetryCount() >= 3) {
            throw new BadRequestException("Maximum retry attempts exceeded");
        }

        payment.setRetryCount(payment.getRetryCount() + 1);
        payment.setLastRetryAt(LocalDateTime.now());
        payment.setStatus(PaymentStatus.PENDING);

        try {
            String dodoPaymentId = createDodoPayment(payment);
            payment.setDodoPaymentId(dodoPaymentId);
            paymentRepository.save(payment);
            log.info("Payment retry initiated for transaction: {}, attempt: {}",
                    payment.getTransactionId(), payment.getRetryCount());
        } catch (Exception e) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Retry failed: " + e.getMessage());
            paymentRepository.save(payment);
            throw new BadRequestException("Payment retry failed: " + e.getMessage());
        }
    }

    private String createDodoPayment(Payment payment) throws Exception {
        String url = dodoPaymentConfig.getBaseUrl() + "/v1/payments";

        Map<String, Object> paymentData = new HashMap<>();
        paymentData.put("amount", payment.getTotalAmount().multiply(BigDecimal.valueOf(100)).longValue()); // Convert to paise
        paymentData.put("currency", payment.getCurrency());
        paymentData.put("reference_id", payment.getTransactionId());
        paymentData.put("idempotency_key", payment.getIdempotencyKey());
        paymentData.put("description", "Subscription payment for " + payment.getCompany().getName());
        paymentData.put("customer_email", payment.getCompany().getEmail());
        paymentData.put("return_url", dodoPaymentConfig.getReturnUrl() + "?txn=" + payment.getTransactionId());
        paymentData.put("cancel_url", dodoPaymentConfig.getCancelUrl() + "?txn=" + payment.getTransactionId());

        // Add metadata for tracking
        Map<String, String> metadata = new HashMap<>();
        metadata.put("company_id", payment.getCompany().getId().toString());
        metadata.put("subscription_id", payment.getSubscription().getId().toString());
        metadata.put("billing_cycle", payment.getBillingCycle().toString());
        paymentData.put("metadata", metadata);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + dodoPaymentConfig.getApiKey());
        headers.set("X-API-Secret", dodoPaymentConfig.getApiSecret());
        headers.set("Idempotency-Key", payment.getIdempotencyKey());

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(paymentData, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED) {
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("payment_id")) {
                return responseBody.get("payment_id").toString();
            }
        }

        throw new Exception("Failed to create Dodo payment");
    }

    private String getClientIp(HttpServletRequest request) {
        if (request == null) return null;

        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private PaymentResponse convertToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .subscriptionId(payment.getSubscription().getId())
                .transactionId(payment.getTransactionId())
                .dodoPaymentId(payment.getDodoPaymentId())
                .amount(payment.getTotalAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .paidAt(payment.getPaidAt())
                .failureReason(payment.getFailureReason())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
