package com.leavemarker.service;

import com.leavemarker.config.RazorpayConfig;
import com.leavemarker.dto.payment.*;
import com.leavemarker.dto.subscription.SubscriptionRequest;
import com.leavemarker.entity.Company;
import com.leavemarker.entity.Payment;
import com.leavemarker.entity.Plan;
import com.leavemarker.entity.Subscription;
import com.leavemarker.enums.BillingCycle;
import com.leavemarker.enums.PaymentStatus;
import com.leavemarker.enums.PaymentType;
import com.leavemarker.enums.PlanTier;
import com.leavemarker.enums.SubscriptionStatus;
import com.leavemarker.exception.BadRequestException;
import com.leavemarker.exception.ResourceNotFoundException;
import com.leavemarker.repository.CompanyRepository;
import com.leavemarker.repository.EmployeeRepository;
import com.leavemarker.repository.PaymentRepository;
import com.leavemarker.repository.PlanRepository;
import com.leavemarker.repository.SubscriptionRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final CompanyRepository companyRepository;
    private final PlanRepository planRepository;
    private final EmployeeRepository employeeRepository;
    private final RazorpayConfig razorpayConfig;
    private final RazorpayClient razorpayClient;
    private final SubscriptionService subscriptionService;

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

        Plan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found with id: " + request.getPlanId()));

        if (!plan.getActive()) {
            throw new BadRequestException("Selected plan is not active");
        }

        if (plan.getTier() == PlanTier.FREE) {
            throw new BadRequestException("Free plan does not require payment");
        }

        // Check if company already has an active subscription to the same plan
        subscriptionRepository.findByCompanyAndStatus(company, SubscriptionStatus.ACTIVE)
                .ifPresent(existingSubscription -> {
                    if (existingSubscription.getPlan().getId().equals(plan.getId())) {
                        throw new BadRequestException("Company already has an active subscription to this plan");
                    }
                });

        // Generate unique IDs
        String transactionId = "TXN-" + UUID.randomUUID().toString();
        String idempotencyKey = "IDEM-" + UUID.randomUUID().toString();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime periodEnd = request.getBillingCycle() == BillingCycle.YEARLY
                ? now.plusYears(1)
                : now.plusMonths(1);

        // Calculate amount based on per-employee pricing
        long employeeCount = employeeRepository.countByCompanyIdAndDeletedFalse(companyId);
        if (employeeCount == 0) {
            employeeCount = 1; // Minimum 1 employee for pricing
        }

        BigDecimal pricePerEmployee = request.getBillingCycle() == BillingCycle.YEARLY
                ? plan.getYearlyPrice()
                : plan.getMonthlyPrice();
        BigDecimal amount = pricePerEmployee.multiply(BigDecimal.valueOf(employeeCount));

        // Create payment record WITHOUT subscription (subscription created after payment success)
        Payment payment = Payment.builder()
                .plan(plan)
                .company(company)
                .transactionId(transactionId)
                .paymentType(PaymentType.NEW_SUBSCRIPTION)
                .billingCycle(request.getBillingCycle())
                .amount(amount)
                .taxAmount(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(amount)
                .currency("INR")
                .status(PaymentStatus.PENDING)
                .initiatedAt(now)
                .periodStart(now)
                .periodEnd(periodEnd)
                .idempotencyKey(idempotencyKey)
                .ipAddress(getClientIp(httpRequest))
                .userAgent(httpRequest != null ? httpRequest.getHeader("User-Agent") : null)
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        try {
            // Create Razorpay Order
            String razorpayOrderId = createRazorpayOrder(savedPayment);
            savedPayment.setRazorpayOrderId(razorpayOrderId);
            paymentRepository.save(savedPayment);

            log.info("Payment initiated for company: {}, plan: {}, transaction: {}, razorpayOrderId: {}",
                    companyId, plan.getName(), transactionId, razorpayOrderId);

            return PaymentInitiateResponse.builder()
                    .razorpayOrderId(razorpayOrderId)
                    .razorpayKeyId(razorpayConfig.getKeyId())
                    .amount(savedPayment.getTotalAmount().multiply(BigDecimal.valueOf(100)).longValue())
                    .currency(savedPayment.getCurrency())
                    .transactionId(transactionId)
                    .companyName(company.getName())
                    .companyEmail(company.getEmail())
                    .employeeCount(employeeCount)
                    .pricePerEmployee(pricePerEmployee.multiply(BigDecimal.valueOf(100)).longValue())
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
    public PaymentResponse verifyAndCompletePayment(PaymentVerifyRequest verifyRequest) {
        Payment payment = paymentRepository.findByRazorpayOrderId(verifyRequest.getRazorpayOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with razorpayOrderId: " + verifyRequest.getRazorpayOrderId()));

        LocalDateTime now = LocalDateTime.now();

        try {
            // Verify signature
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", verifyRequest.getRazorpayOrderId());
            options.put("razorpay_payment_id", verifyRequest.getRazorpayPaymentId());
            options.put("razorpay_signature", verifyRequest.getRazorpaySignature());

            boolean isValidSignature = Utils.verifyPaymentSignature(options, razorpayConfig.getKeySecret());

            if (!isValidSignature) {
                payment.setStatus(PaymentStatus.FAILED);
                payment.setFailedAt(now);
                payment.setFailureReason("Invalid payment signature");
                paymentRepository.save(payment);
                throw new BadRequestException("Invalid payment signature");
            }

            // Update payment with Razorpay details
            payment.setRazorpayPaymentId(verifyRequest.getRazorpayPaymentId());
            payment.setRazorpaySignature(verifyRequest.getRazorpaySignature());
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setPaidAt(now);

            // Create subscription ONLY after successful payment verification
            SubscriptionRequest subscriptionRequest = new SubscriptionRequest();
            subscriptionRequest.setPlanId(payment.getPlan().getId());
            subscriptionRequest.setBillingCycle(payment.getBillingCycle());
            subscriptionRequest.setAutoRenew(true);

            var subscriptionResponse = subscriptionService.createSubscription(
                    payment.getCompany().getId(), subscriptionRequest);

            // Link the subscription to the payment
            Subscription subscription = subscriptionRepository.findById(subscriptionResponse.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));
            subscription.setIsPaid(true);
            subscription.setCurrentPeriodEnd(payment.getPeriodEnd());
            subscription.setEndDate(payment.getPeriodEnd());
            subscriptionRepository.save(subscription);

            payment.setSubscription(subscription);
            paymentRepository.save(payment);

            log.info("Payment verified and subscription created for transaction: {}, razorpayPaymentId: {}",
                    payment.getTransactionId(), verifyRequest.getRazorpayPaymentId());

            return convertToResponse(payment);

        } catch (RazorpayException e) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailedAt(now);
            payment.setFailureReason("Signature verification failed: " + e.getMessage());
            paymentRepository.save(payment);
            log.error("Payment verification failed for transaction: {}", payment.getTransactionId(), e);
            throw new BadRequestException("Payment verification failed: " + e.getMessage());
        }
    }

    @Transactional
    public void handleWebhook(String rawPayload, String razorpaySignature) {
        try {
            // Verify webhook signature
            boolean isValid = Utils.verifyWebhookSignature(rawPayload, razorpaySignature, razorpayConfig.getWebhookSecret());

            if (!isValid) {
                log.warn("Invalid webhook signature received");
                throw new BadRequestException("Invalid webhook signature");
            }

            JSONObject webhookPayload = new JSONObject(rawPayload);
            String event = webhookPayload.getString("event");
            JSONObject payloadData = webhookPayload.getJSONObject("payload");
            JSONObject paymentEntity = payloadData.getJSONObject("payment").getJSONObject("entity");

            String razorpayOrderId = paymentEntity.getString("order_id");
            String razorpayPaymentId = paymentEntity.getString("id");

            Payment payment = paymentRepository.findByRazorpayOrderId(razorpayOrderId)
                    .orElse(null);

            if (payment == null) {
                log.warn("Payment not found for razorpayOrderId: {}", razorpayOrderId);
                return;
            }

            LocalDateTime now = LocalDateTime.now();
            payment.setWebhookStatus(event);
            payment.setWebhookReceivedAt(now);
            payment.setWebhookPayload(rawPayload);
            payment.setRazorpayPaymentId(razorpayPaymentId);

            switch (event) {
                case "payment.captured" -> {
                    if (payment.getStatus() != PaymentStatus.SUCCESS) {
                        payment.setStatus(PaymentStatus.SUCCESS);
                        payment.setPaidAt(now);

                        String method = paymentEntity.optString("method", null);
                        payment.setPaymentMethod(method);

                        // Create subscription if not exists (webhook came before verify call)
                        if (payment.getSubscription() == null) {
                            SubscriptionRequest subscriptionRequest = new SubscriptionRequest();
                            subscriptionRequest.setPlanId(payment.getPlan().getId());
                            subscriptionRequest.setBillingCycle(payment.getBillingCycle());
                            subscriptionRequest.setAutoRenew(true);

                            var subscriptionResponse = subscriptionService.createSubscription(
                                    payment.getCompany().getId(), subscriptionRequest);

                            Subscription subscription = subscriptionRepository.findById(subscriptionResponse.getId())
                                    .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));
                            subscription.setIsPaid(true);
                            subscription.setCurrentPeriodEnd(payment.getPeriodEnd());
                            subscription.setEndDate(payment.getPeriodEnd());
                            subscriptionRepository.save(subscription);

                            payment.setSubscription(subscription);
                        } else {
                            // Update existing subscription
                            Subscription subscription = payment.getSubscription();
                            subscription.setIsPaid(true);
                            subscription.setCurrentPeriodEnd(payment.getPeriodEnd());
                            subscription.setEndDate(payment.getPeriodEnd());
                            subscription.setStatus(SubscriptionStatus.ACTIVE);
                            subscriptionRepository.save(subscription);
                        }

                        log.info("Payment captured via webhook for transaction: {}", payment.getTransactionId());
                    }
                }
                case "payment.failed" -> {
                    payment.setStatus(PaymentStatus.FAILED);
                    payment.setFailedAt(now);

                    JSONObject errorObj = paymentEntity.optJSONObject("error_description");
                    String errorReason = errorObj != null ? errorObj.optString("description", "Payment failed") : "Payment failed";
                    payment.setFailureReason(errorReason);

                    log.warn("Payment failed via webhook for transaction: {}", payment.getTransactionId());
                }
                case "refund.created" -> {
                    payment.setStatus(PaymentStatus.REFUNDED);
                    payment.setRefundedAt(now);
                    log.info("Payment refunded via webhook for transaction: {}", payment.getTransactionId());
                }
                default -> log.info("Received webhook event: {} for transaction: {}", event, payment.getTransactionId());
            }

            paymentRepository.save(payment);

        } catch (RazorpayException e) {
            log.error("Webhook signature verification failed", e);
            throw new BadRequestException("Invalid webhook signature");
        } catch (Exception e) {
            log.error("Error processing webhook", e);
            throw new BadRequestException("Error processing webhook: " + e.getMessage());
        }
    }

    @Transactional
    public PaymentInitiateResponse retryPayment(Long paymentId) {
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
            String razorpayOrderId = createRazorpayOrder(payment);
            payment.setRazorpayOrderId(razorpayOrderId);
            paymentRepository.save(payment);

            log.info("Payment retry initiated for transaction: {}, attempt: {}",
                    payment.getTransactionId(), payment.getRetryCount());

            return PaymentInitiateResponse.builder()
                    .razorpayOrderId(razorpayOrderId)
                    .razorpayKeyId(razorpayConfig.getKeyId())
                    .amount(payment.getTotalAmount().multiply(BigDecimal.valueOf(100)).longValue())
                    .currency(payment.getCurrency())
                    .transactionId(payment.getTransactionId())
                    .companyName(payment.getCompany().getName())
                    .companyEmail(payment.getCompany().getEmail())
                    .build();

        } catch (Exception e) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Retry failed: " + e.getMessage());
            paymentRepository.save(payment);
            throw new BadRequestException("Payment retry failed: " + e.getMessage());
        }
    }

    private String createRazorpayOrder(Payment payment) throws RazorpayException {
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", payment.getTotalAmount().multiply(BigDecimal.valueOf(100)).longValue()); // Convert to paise
        orderRequest.put("currency", payment.getCurrency());
        orderRequest.put("receipt", payment.getTransactionId());

        // Add notes for tracking
        JSONObject notes = new JSONObject();
        notes.put("company_id", payment.getCompany().getId().toString());
        notes.put("plan_id", payment.getPlan().getId().toString());
        notes.put("billing_cycle", payment.getBillingCycle().toString());
        notes.put("transaction_id", payment.getTransactionId());
        orderRequest.put("notes", notes);

        Order order = razorpayClient.orders.create(orderRequest);
        return order.get("id");
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
                .subscriptionId(payment.getSubscription() != null ? payment.getSubscription().getId() : null)
                .transactionId(payment.getTransactionId())
                .razorpayOrderId(payment.getRazorpayOrderId())
                .razorpayPaymentId(payment.getRazorpayPaymentId())
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
