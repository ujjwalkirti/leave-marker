package com.leavemarker.controller;

import com.leavemarker.dto.ApiResponse;
import com.leavemarker.dto.subscription.SubscriptionFeatureResponse;
import com.leavemarker.dto.subscription.SubscriptionRequest;
import com.leavemarker.dto.subscription.SubscriptionResponse;
import com.leavemarker.security.UserPrincipal;
import com.leavemarker.service.SubscriptionFeatureService;
import com.leavemarker.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final SubscriptionFeatureService subscriptionFeatureService;

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getActiveSubscription(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        SubscriptionResponse subscription = subscriptionService.getActiveSubscription(userPrincipal.getCompanyId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Active subscription retrieved successfully", subscription));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<SubscriptionResponse>>> getCompanySubscriptions(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<SubscriptionResponse> subscriptions = subscriptionService.getCompanySubscriptions(userPrincipal.getCompanyId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Subscriptions retrieved successfully", subscriptions));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> createSubscription(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody SubscriptionRequest request) {
        SubscriptionResponse subscription = subscriptionService.createSubscription(userPrincipal.getCompanyId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Subscription created successfully", subscription));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> updateSubscription(
            @PathVariable Long id,
            @Valid @RequestBody SubscriptionRequest request) {
        SubscriptionResponse subscription = subscriptionService.updateSubscription(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Subscription updated successfully", subscription));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> cancelSubscription(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        subscriptionService.cancelSubscription(id, reason);
        return ResponseEntity.ok(new ApiResponse<>(true, "Subscription cancelled successfully", null));
    }

    @GetMapping("/features")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<ApiResponse<SubscriptionFeatureResponse>> getSubscriptionFeatures(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        SubscriptionFeatureResponse features = subscriptionFeatureService.getFeatureResponse(userPrincipal.getCompanyId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Subscription features retrieved successfully", features));
    }
}
