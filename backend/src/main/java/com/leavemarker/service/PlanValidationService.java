package com.leavemarker.service;

import com.leavemarker.entity.Plan;
import com.leavemarker.entity.Subscription;
import com.leavemarker.enums.SubscriptionStatus;
import com.leavemarker.exception.BadRequestException;
import com.leavemarker.repository.EmployeeRepository;
import com.leavemarker.repository.HolidayRepository;
import com.leavemarker.repository.LeavePolicyRepository;
import com.leavemarker.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlanValidationService {

    private final SubscriptionRepository subscriptionRepository;
    private final EmployeeRepository employeeRepository;
    private final LeavePolicyRepository leavePolicyRepository;
    private final HolidayRepository holidayRepository;

    /**
     * Validates if the company can add more employees based on their plan
     */
    public void validateEmployeeLimit(Long companyId) {
        Subscription subscription = getActiveSubscription(companyId);
        Plan plan = subscription.getPlan();

        long currentEmployeeCount = employeeRepository.countByCompanyIdAndDeletedFalse(companyId);

        // For FREE plan, max 10 employees
        if (plan.getMaxEmployees() > 0 && currentEmployeeCount >= plan.getMaxEmployees()) {
            throw new BadRequestException(
                    String.format("Employee limit reached. Your current plan allows up to %d employees. Please upgrade to add more employees.",
                            plan.getMaxEmployees()));
        }
    }

    /**
     * Validates if the company can add more leave policies based on their plan
     */
    public void validateLeavePolicyLimit(Long companyId) {
        Subscription subscription = getActiveSubscription(companyId);
        Plan plan = subscription.getPlan();

        // For FREE plan, only 1 leave policy allowed
        if (!plan.getMultipleLeavePolicies()) {
            long activePolicyCount = leavePolicyRepository.countByCompanyIdAndActiveAndDeletedFalse(companyId, true);

            if (activePolicyCount >= plan.getMaxLeavePolicies()) {
                throw new BadRequestException(
                        String.format("Leave policy limit reached. Your current plan allows only %d active leave policy. Please upgrade to Mid Tier plan for multiple leave policies.",
                                plan.getMaxLeavePolicies()));
            }
        }
        // MID_TIER allows unlimited policies (no check needed)
    }

    /**
     * Validates if the company can add more holidays based on their plan
     */
    public void validateHolidayLimit(Long companyId) {
        Subscription subscription = getActiveSubscription(companyId);
        Plan plan = subscription.getPlan();

        // For FREE plan, max 6 holidays
        if (!plan.getUnlimitedHolidays()) {
            long holidayCount = holidayRepository.countByCompanyIdAndDeletedFalse(companyId);

            if (holidayCount >= plan.getMaxHolidays()) {
                throw new BadRequestException(
                        String.format("Holiday limit reached. Your current plan allows up to %d holidays. Please upgrade to Mid Tier plan for unlimited holidays.",
                                plan.getMaxHolidays()));
            }
        }
        // MID_TIER allows unlimited holidays (no check needed)
    }

    /**
     * Validates if the company has access to attendance management
     */
    public void validateAttendanceManagementAccess(Long companyId) {
        Subscription subscription = getActiveSubscription(companyId);
        Plan plan = subscription.getPlan();

        if (!plan.getAttendanceManagement()) {
            throw new BadRequestException(
                    "Attendance management is not available in your current plan. Please upgrade to Mid Tier plan to access this feature.");
        }
    }

    /**
     * Validates if the company has access to reports download
     */
    public void validateReportsDownloadAccess(Long companyId) {
        Subscription subscription = getActiveSubscription(companyId);
        Plan plan = subscription.getPlan();

        if (!plan.getReportsDownload() && !subscription.getHasReportDownloadAddon()) {
            throw new BadRequestException(
                    "Report downloads are not available in your current plan. Please upgrade to Mid Tier plan or purchase the report download add-on.");
        }
    }

    /**
     * Validates if the company has access to attendance rate analytics
     */
    public void validateAttendanceRateAnalyticsAccess(Long companyId) {
        Subscription subscription = getActiveSubscription(companyId);
        Plan plan = subscription.getPlan();

        if (!plan.getAttendanceRateAnalytics()) {
            throw new BadRequestException(
                    "Attendance rate analytics is not available in your current plan. Please upgrade to Mid Tier plan to access this feature.");
        }
    }

    /**
     * Gets the active subscription for a company
     * Throws exception if no active subscription found
     */
    private Subscription getActiveSubscription(Long companyId) {
        return subscriptionRepository.findByCompanyIdAndStatus(companyId, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new BadRequestException("No active subscription found. Please subscribe to a plan first."));
    }
}
