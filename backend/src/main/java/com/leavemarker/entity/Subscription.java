package com.leavemarker.entity;

import com.leavemarker.enums.BillingCycle;
import com.leavemarker.enums.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BillingCycle billingCycle;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Column(nullable = false)
    private LocalDateTime currentPeriodStart;

    @Column(nullable = false)
    private LocalDateTime currentPeriodEnd;

    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private Boolean autoRenew = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isPaid = false;

    // Add-on flags
    @Column(nullable = false)
    @Builder.Default
    private Boolean hasReportDownloadAddon = false;

    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal reportDownloadAddonPrice = BigDecimal.ZERO;

    @Column(length = 500)
    private String cancellationReason;

    private LocalDateTime cancelledAt;

    @Column(length = 500)
    private String notes;
}
