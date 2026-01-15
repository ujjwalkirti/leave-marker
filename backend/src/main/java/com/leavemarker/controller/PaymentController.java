package com.leavemarker.controller;

import com.leavemarker.dto.ApiResponse;
import com.leavemarker.dto.payment.PaymentInitiateRequest;
import com.leavemarker.dto.payment.PaymentInitiateResponse;
import com.leavemarker.dto.payment.PaymentResponse;
import com.leavemarker.dto.payment.PaymentVerifyRequest;
import com.leavemarker.security.UserPrincipal;
import com.leavemarker.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getCompanyPayments(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<PaymentResponse> payments = paymentService.getCompanyPayments(userPrincipal.getCompanyId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Payments retrieved successfully", payments));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(@PathVariable Long id) {
        PaymentResponse payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Payment retrieved successfully", payment));
    }

    @PostMapping("/initiate")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PaymentInitiateResponse>> initiatePayment(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody PaymentInitiateRequest request,
            HttpServletRequest httpRequest) {
        PaymentInitiateResponse response = paymentService.initiatePayment(
                userPrincipal.getCompanyId(), request, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Payment initiated successfully", response));
    }

    @PostMapping("/verify")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PaymentResponse>> verifyPayment(
            @Valid @RequestBody PaymentVerifyRequest request) {
        PaymentResponse response = paymentService.verifyAndCompletePayment(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Payment verified successfully", response));
    }

    @PostMapping("/webhook")
    public ResponseEntity<ApiResponse<Void>> handleWebhook(
            @RequestBody String rawPayload,
            @RequestHeader("X-Razorpay-Signature") String razorpaySignature) {
        try {
            log.info("Received Razorpay webhook");
            paymentService.handleWebhook(rawPayload, razorpaySignature);
            return ResponseEntity.ok(new ApiResponse<>(true, "Webhook processed successfully", null));
        } catch (Exception e) {
            log.error("Failed to process webhook", e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Failed to process webhook: " + e.getMessage(), null));
        }
    }

    @PostMapping("/{id}/retry")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PaymentInitiateResponse>> retryPayment(@PathVariable Long id) {
        PaymentInitiateResponse response = paymentService.retryPayment(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Payment retry initiated successfully", response));
    }
}
