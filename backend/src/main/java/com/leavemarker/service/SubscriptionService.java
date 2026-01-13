package com.leavemarker.service;

import com.leavemarker.dto.plan.PlanResponse;
import com.leavemarker.dto.subscription.SubscriptionRequest;
import com.leavemarker.dto.subscription.SubscriptionResponse;
import com.leavemarker.entity.Company;
import com.leavemarker.entity.Plan;
import com.leavemarker.entity.Subscription;
import com.leavemarker.enums.BillingCycle;
import com.leavemarker.enums.PlanTier;
import com.leavemarker.enums.SubscriptionStatus;
import com.leavemarker.exception.BadRequestException;
import com.leavemarker.exception.ResourceNotFoundException;
import com.leavemarker.repository.CompanyRepository;
import com.leavemarker.repository.PlanRepository;
import com.leavemarker.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;
    private final CompanyRepository companyRepository;

    @Transactional(readOnly = true)
    public SubscriptionResponse getActiveSubscription(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));

        Subscription subscription = subscriptionRepository
                .findByCompanyAndStatus(company, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("No active subscription found for company"));

        return convertToResponse(subscription);
    }

    @Transactional(readOnly = true)
    public Optional<SubscriptionResponse> getActiveSubscriptionOptional(Long companyId) {
        Company company = companyRepository.findById(companyId).orElse(null);
        if (company == null) return Optional.empty();

        return subscriptionRepository
                .findByCompanyAndStatus(company, SubscriptionStatus.ACTIVE)
                .map(this::convertToResponse);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getCompanySubscriptions(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));

        return subscriptionRepository.findByCompany(company).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public SubscriptionResponse createSubscription(Long companyId, SubscriptionRequest request) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));

        Plan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found with id: " + request.getPlanId()));

        if (!plan.getActive()) {
            throw new BadRequestException("Selected plan is not active");
        }

        // Cancel existing active subscription if any
        subscriptionRepository.findByCompanyAndStatus(company, SubscriptionStatus.ACTIVE)
                .ifPresent(existingSubscription -> {
                    existingSubscription.setStatus(SubscriptionStatus.CANCELLED);
                    existingSubscription.setCancelledAt(LocalDateTime.now());
                    existingSubscription.setCancellationReason("Upgraded/changed to new plan");
                    subscriptionRepository.save(existingSubscription);
                    log.info("Cancelled existing subscription for company: {}", companyId);
                });

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime periodEnd = request.getBillingCycle() == BillingCycle.YEARLY
                ? now.plusYears(1)
                : now.plusMonths(1);

        BigDecimal amount = request.getBillingCycle() == BillingCycle.YEARLY
                ? plan.getYearlyPrice()
                : plan.getMonthlyPrice();

        // For FREE tier, subscription is immediately active and paid
        boolean isPaid = plan.getTier() == PlanTier.FREE;
        SubscriptionStatus status = isPaid ? SubscriptionStatus.ACTIVE : SubscriptionStatus.ACTIVE;

        Subscription subscription = Subscription.builder()
                .company(company)
                .plan(plan)
                .status(status)
                .billingCycle(request.getBillingCycle())
                .startDate(now)
                .endDate(periodEnd)
                .currentPeriodStart(now)
                .currentPeriodEnd(periodEnd)
                .amount(amount)
                .autoRenew(request.getAutoRenew() != null ? request.getAutoRenew() : true)
                .isPaid(isPaid)
                .notes(request.getNotes())
                .build();

        Subscription savedSubscription = subscriptionRepository.save(subscription);
        log.info("Created subscription for company: {} with plan: {} (tier: {})",
                companyId, plan.getName(), plan.getTier());
        return convertToResponse(savedSubscription);
    }

    @Transactional
    public SubscriptionResponse updateSubscription(Long subscriptionId, SubscriptionRequest request) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + subscriptionId));

        Plan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found with id: " + request.getPlanId()));

        if (!plan.getActive()) {
            throw new BadRequestException("Selected plan is not active");
        }

        BigDecimal amount = request.getBillingCycle() == BillingCycle.YEARLY
                ? plan.getYearlyPrice()
                : plan.getMonthlyPrice();

        subscription.setPlan(plan);
        subscription.setBillingCycle(request.getBillingCycle());
        subscription.setAmount(amount);
        subscription.setAutoRenew(request.getAutoRenew() != null ? request.getAutoRenew() : true);
        subscription.setNotes(request.getNotes());

        // If upgrading, may need to handle payment
        if (plan.getTier() != PlanTier.FREE && !subscription.getIsPaid()) {
            subscription.setIsPaid(false);
        }

        Subscription updatedSubscription = subscriptionRepository.save(subscription);
        log.info("Updated subscription: {}", subscriptionId);
        return convertToResponse(updatedSubscription);
    }

    @Transactional
    public void cancelSubscription(Long subscriptionId, String reason) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + subscriptionId));

        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription.setCancelledAt(LocalDateTime.now());
        subscription.setCancellationReason(reason);
        subscriptionRepository.save(subscription);
        log.info("Cancelled subscription: {}", subscriptionId);
    }

    @Transactional
    public void expireSubscriptions() {
        List<Subscription> expiredSubscriptions = subscriptionRepository
                .findByEndDateBeforeAndStatus(LocalDateTime.now(), SubscriptionStatus.ACTIVE);

        for (Subscription subscription : expiredSubscriptions) {
            subscription.setStatus(SubscriptionStatus.EXPIRED);
            subscriptionRepository.save(subscription);
            log.info("Expired subscription: {} for company: {}",
                    subscription.getId(), subscription.getCompany().getId());
        }
    }

    @Transactional
    public void markAsPaid(Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));
        subscription.setIsPaid(true);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscriptionRepository.save(subscription);
        log.info("Marked subscription {} as paid", subscriptionId);
    }

    private SubscriptionResponse convertToResponse(Subscription subscription) {
        return SubscriptionResponse.builder()
                .id(subscription.getId())
                .companyId(subscription.getCompany().getId())
                .plan(convertPlanToResponse(subscription.getPlan()))
                .status(subscription.getStatus())
                .billingCycle(subscription.getBillingCycle())
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .currentPeriodStart(subscription.getCurrentPeriodStart())
                .currentPeriodEnd(subscription.getCurrentPeriodEnd())
                .amount(subscription.getAmount())
                .autoRenew(subscription.getAutoRenew())
                .isPaid(subscription.getIsPaid())
                .notes(subscription.getNotes())
                .build();
    }

    private PlanResponse convertPlanToResponse(Plan plan) {
        return PlanResponse.builder()
                .id(plan.getId())
                .name(plan.getName())
                .description(plan.getDescription())
                .tier(plan.getTier())
                .billingCycle(plan.getBillingCycle())
                .monthlyPrice(plan.getMonthlyPrice())
                .yearlyPrice(plan.getYearlyPrice())
                .minEmployees(plan.getMinEmployees())
                .maxEmployees(plan.getMaxEmployees())
                .maxLeavePolicies(plan.getMaxLeavePolicies())
                .maxHolidays(plan.getMaxHolidays())
                .active(plan.getActive())
                .attendanceManagement(plan.getAttendanceManagement())
                .reportsDownload(plan.getReportsDownload())
                .multipleLeavePolicies(plan.getMultipleLeavePolicies())
                .unlimitedHolidays(plan.getUnlimitedHolidays())
                .attendanceRateAnalytics(plan.getAttendanceRateAnalytics())
                .reportDownloadPriceUnder50(plan.getReportDownloadPriceUnder50())
                .reportDownloadPrice50Plus(plan.getReportDownloadPrice50Plus())
                .build();
    }
}
