package com.leavemarker.service;

import com.leavemarker.dto.subscription.SubscriptionFeatureResponse;
import com.leavemarker.entity.Company;
import com.leavemarker.entity.Plan;
import com.leavemarker.entity.Subscription;
import com.leavemarker.enums.PlanTier;
import com.leavemarker.enums.SubscriptionStatus;
import com.leavemarker.exception.BadRequestException;
import com.leavemarker.repository.CompanyRepository;
import com.leavemarker.repository.EmployeeRepository;
import com.leavemarker.repository.LeavePolicyRepository;
import com.leavemarker.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionFeatureService {

    private final SubscriptionRepository subscriptionRepository;
    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;
    private final LeavePolicyRepository leavePolicyRepository;

    /**
     * Get the current active subscription for a company.
     * Returns a default FREE plan config if no subscription exists.
     */
    @Transactional(readOnly = true)
    public SubscriptionInfo getSubscriptionInfo(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BadRequestException("Company not found"));

        Optional<Subscription> subscriptionOpt = subscriptionRepository
                .findByCompanyAndStatus(company, SubscriptionStatus.ACTIVE);

        if (subscriptionOpt.isEmpty()) {
            // Return default FREE tier limits
            return SubscriptionInfo.builder()
                    .activeSubscription(false)
                    .tier(PlanTier.FREE)
                    .maxEmployees(10)
                    .maxLeavePolicies(1)
                    .maxHolidays(6)
                    .attendanceManagement(false)
                    .reportsDownload(false)
                    .multipleLeavePolicies(false)
                    .unlimitedHolidays(false)
                    .attendanceRateAnalytics(false)
                    .build();
        }

        Subscription subscription = subscriptionOpt.get();
        Plan plan = subscription.getPlan();

        // Check if subscription is within valid period
        boolean isValidPeriod = LocalDateTime.now().isBefore(subscription.getCurrentPeriodEnd());

        return SubscriptionInfo.builder()
                .activeSubscription(true)
                .subscriptionId(subscription.getId())
                .paid(subscription.getIsPaid())
                .valid(isValidPeriod)
                .tier(plan.getTier())
                .planName(plan.getName())
                .maxEmployees(plan.getMaxEmployees())
                .maxLeavePolicies(plan.getMaxLeavePolicies())
                .maxHolidays(plan.getMaxHolidays())
                .attendanceManagement(plan.getAttendanceManagement())
                .reportsDownload(plan.getReportsDownload())
                .multipleLeavePolicies(plan.getMultipleLeavePolicies())
                .unlimitedHolidays(plan.getUnlimitedHolidays())
                .attendanceRateAnalytics(plan.getAttendanceRateAnalytics())
                .currentPeriodEnd(subscription.getCurrentPeriodEnd())
                .build();
    }

    /**
     * Check if company can add more employees.
     */
    @Transactional(readOnly = true)
    public void validateCanAddEmployee(Long companyId) {
        SubscriptionInfo info = getSubscriptionInfo(companyId);
        long currentEmployeeCount = employeeRepository.countByCompanyIdAndDeletedFalse(companyId);

        if (currentEmployeeCount >= info.getMaxEmployees()) {
            throw new BadRequestException(
                    String.format("Employee limit reached. Your %s plan allows maximum %d employees. " +
                                    "Please upgrade to add more employees.",
                            info.getTier().name(), info.getMaxEmployees()));
        }
    }

    /**
     * Check if company can add more leave policies.
     */
    @Transactional(readOnly = true)
    public void validateCanAddLeavePolicy(Long companyId) {
        SubscriptionInfo info = getSubscriptionInfo(companyId);
        long currentPolicyCount = leavePolicyRepository.countByCompanyIdAndDeletedFalse(companyId);

        if (currentPolicyCount >= info.getMaxLeavePolicies()) {
            throw new BadRequestException(
                    String.format("Leave policy limit reached. Your %s plan allows maximum %d leave policies. " +
                                    "Please upgrade to add more.",
                            info.getTier().name(), info.getMaxLeavePolicies()));
        }
    }

    /**
     * Check if company has access to attendance management.
     */
    @Transactional(readOnly = true)
    public void validateAttendanceAccess(Long companyId) {
        SubscriptionInfo info = getSubscriptionInfo(companyId);

        if (!info.isAttendanceManagement()) {
            throw new BadRequestException(
                    "Attendance management is not available on the FREE plan. Please upgrade to access this feature.");
        }
    }

    /**
     * Check if company has access to reports download.
     */
    @Transactional(readOnly = true)
    public void validateReportsAccess(Long companyId) {
        SubscriptionInfo info = getSubscriptionInfo(companyId);

        if (!info.isReportsDownload()) {
            throw new BadRequestException(
                    "Reports download is not available on the FREE plan. Please upgrade to access this feature.");
        }
    }

    /**
     * Check if company has access to attendance rate analytics.
     */
    @Transactional(readOnly = true)
    public boolean hasAttendanceRateAnalyticsAccess(Long companyId) {
        return getSubscriptionInfo(companyId).isAttendanceRateAnalytics();
    }

    /**
     * Get remaining employee slots.
     */
    @Transactional(readOnly = true)
    public int getRemainingEmployeeSlots(Long companyId) {
        SubscriptionInfo info = getSubscriptionInfo(companyId);
        long currentCount = employeeRepository.countByCompanyIdAndDeletedFalse(companyId);
        return Math.max(0, info.getMaxEmployees() - (int) currentCount);
    }

    /**
     * Get remaining leave policy slots.
     */
    @Transactional(readOnly = true)
    public int getRemainingLeavePolicySlots(Long companyId) {
        SubscriptionInfo info = getSubscriptionInfo(companyId);
        long currentCount = leavePolicyRepository.countByCompanyIdAndDeletedFalse(companyId);
        return Math.max(0, info.getMaxLeavePolicies() - (int) currentCount);
    }

    /**
     * Get full subscription feature response for frontend.
     */
    @Transactional(readOnly = true)
    public SubscriptionFeatureResponse getFeatureResponse(Long companyId) {
        SubscriptionInfo info = getSubscriptionInfo(companyId);
        int currentEmployees = (int) employeeRepository.countByCompanyIdAndDeletedFalse(companyId);
        int currentPolicies = (int) leavePolicyRepository.countByCompanyIdAndDeletedFalse(companyId);

        return SubscriptionFeatureResponse.builder()
                .hasActiveSubscription(info.isActiveSubscription())
                .subscriptionId(info.getSubscriptionId())
                .isPaid(info.isPaid())
                .isValid(info.isValid())
                .tier(info.getTier())
                .planName(info.getPlanName())
                .maxEmployees(info.getMaxEmployees())
                .currentEmployees(currentEmployees)
                .remainingEmployeeSlots(Math.max(0, info.getMaxEmployees() - currentEmployees))
                .maxLeavePolicies(info.getMaxLeavePolicies())
                .currentLeavePolicies(currentPolicies)
                .remainingLeavePolicySlots(Math.max(0, info.getMaxLeavePolicies() - currentPolicies))
                .maxHolidays(info.getMaxHolidays())
                .attendanceManagement(info.isAttendanceManagement())
                .reportsDownload(info.isReportsDownload())
                .multipleLeavePolicies(info.isMultipleLeavePolicies())
                .unlimitedHolidays(info.isUnlimitedHolidays())
                .attendanceRateAnalytics(info.isAttendanceRateAnalytics())
                .currentPeriodEnd(info.getCurrentPeriodEnd())
                .build();
    }

    @lombok.Builder
    @lombok.Getter
    public static class SubscriptionInfo {
        private final boolean activeSubscription;
        private final Long subscriptionId;
        private final boolean paid;
        private final boolean valid;
        private final PlanTier tier;
        private final String planName;
        private final int maxEmployees;
        private final int maxLeavePolicies;
        private final int maxHolidays;
        private final boolean attendanceManagement;
        private final boolean reportsDownload;
        private final boolean multipleLeavePolicies;
        private final boolean unlimitedHolidays;
        private final boolean attendanceRateAnalytics;
        private final LocalDateTime currentPeriodEnd;
    }
}
