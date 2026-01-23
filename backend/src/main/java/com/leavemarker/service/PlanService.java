package com.leavemarker.service;

import com.leavemarker.dto.plan.PlanRequest;
import com.leavemarker.dto.plan.PlanResponse;
import com.leavemarker.entity.Plan;
import com.leavemarker.enums.PlanTier;
import com.leavemarker.enums.PlanType;
import com.leavemarker.exception.ResourceNotFoundException;
import com.leavemarker.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlanService {

    private final PlanRepository planRepository;

    @Transactional(readOnly = true)
    public List<PlanResponse> getAllPlans() {
        return planRepository.findAll().stream()
                .filter(p -> !p.getDeleted())
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PlanResponse> getActivePlans() {
        return planRepository.findByActiveTrue().stream()
                .filter(p -> !p.getDeleted())
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PlanResponse> getPlansByTier(PlanTier tier) {
        return planRepository.findByTierAndActiveTrue(tier).stream()
                .filter(p -> !p.getDeleted())
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PlanResponse getPlanById(Long id) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found with id: " + id));
        return convertToResponse(plan);
    }

    @Transactional
    public PlanResponse createPlan(PlanRequest request) {
        Plan plan = Plan.builder()
                .name(request.getName())
                .description(request.getDescription())
                .tier(request.getTier())
                .planType(request.getPlanType() != null ? request.getPlanType() : PlanType.FREE)
                .billingCycle(request.getBillingCycle())
                .monthlyPrice(request.getMonthlyPrice())
                .yearlyPrice(request.getYearlyPrice())
                .minEmployees(request.getMinEmployees() != null ? request.getMinEmployees() : 1)
                .maxEmployees(request.getMaxEmployees() != null ? request.getMaxEmployees() : 10)
                .maxLeavePolicies(request.getMaxLeavePolicies() != null ? request.getMaxLeavePolicies() : 1)
                .maxHolidays(request.getMaxHolidays() != null ? request.getMaxHolidays() : 6)
                .active(request.getActive() != null ? request.getActive() : true)
                .attendanceManagement(request.getAttendanceManagement() != null ? request.getAttendanceManagement() : false)
                .reportsDownload(request.getReportsDownload() != null ? request.getReportsDownload() : false)
                .multipleLeavePolicies(request.getMultipleLeavePolicies() != null ? request.getMultipleLeavePolicies() : false)
                .unlimitedHolidays(request.getUnlimitedHolidays() != null ? request.getUnlimitedHolidays() : false)
                .attendanceRateAnalytics(request.getAttendanceRateAnalytics() != null ? request.getAttendanceRateAnalytics() : false)
                .advancedReports(request.getAdvancedReports() != null ? request.getAdvancedReports() : false)
                .customLeaveTypes(request.getCustomLeaveTypes() != null ? request.getCustomLeaveTypes() : false)
                .apiAccess(request.getApiAccess() != null ? request.getApiAccess() : false)
                .prioritySupport(request.getPrioritySupport() != null ? request.getPrioritySupport() : false)
                .pricePerEmployee(request.getPricePerEmployee() != null ? request.getPricePerEmployee() : java.math.BigDecimal.ZERO)
                .reportDownloadPriceUnder50(request.getReportDownloadPriceUnder50() != null ? request.getReportDownloadPriceUnder50() : java.math.BigDecimal.ZERO)
                .reportDownloadPrice50Plus(request.getReportDownloadPrice50Plus() != null ? request.getReportDownloadPrice50Plus() : java.math.BigDecimal.ZERO)
                .build();

        Plan savedPlan = planRepository.save(plan);
        log.info("Created new plan: {} ({})", savedPlan.getName(), savedPlan.getTier());
        return convertToResponse(savedPlan);
    }

    @Transactional
    public PlanResponse updatePlan(Long id, PlanRequest request) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found with id: " + id));

        plan.setName(request.getName());
        plan.setDescription(request.getDescription());
        plan.setTier(request.getTier());
        plan.setPlanType(request.getPlanType() != null ? request.getPlanType() : PlanType.FREE);
        plan.setBillingCycle(request.getBillingCycle());
        plan.setMonthlyPrice(request.getMonthlyPrice());
        plan.setYearlyPrice(request.getYearlyPrice());
        plan.setMinEmployees(request.getMinEmployees() != null ? request.getMinEmployees() : 1);
        plan.setMaxEmployees(request.getMaxEmployees() != null ? request.getMaxEmployees() : 10);
        plan.setMaxLeavePolicies(request.getMaxLeavePolicies() != null ? request.getMaxLeavePolicies() : 1);
        plan.setMaxHolidays(request.getMaxHolidays() != null ? request.getMaxHolidays() : 6);
        plan.setActive(request.getActive() != null ? request.getActive() : true);
        plan.setAttendanceManagement(request.getAttendanceManagement() != null ? request.getAttendanceManagement() : false);
        plan.setReportsDownload(request.getReportsDownload() != null ? request.getReportsDownload() : false);
        plan.setMultipleLeavePolicies(request.getMultipleLeavePolicies() != null ? request.getMultipleLeavePolicies() : false);
        plan.setUnlimitedHolidays(request.getUnlimitedHolidays() != null ? request.getUnlimitedHolidays() : false);
        plan.setAttendanceRateAnalytics(request.getAttendanceRateAnalytics() != null ? request.getAttendanceRateAnalytics() : false);
        plan.setAdvancedReports(request.getAdvancedReports() != null ? request.getAdvancedReports() : false);
        plan.setCustomLeaveTypes(request.getCustomLeaveTypes() != null ? request.getCustomLeaveTypes() : false);
        plan.setApiAccess(request.getApiAccess() != null ? request.getApiAccess() : false);
        plan.setPrioritySupport(request.getPrioritySupport() != null ? request.getPrioritySupport() : false);
        plan.setPricePerEmployee(request.getPricePerEmployee() != null ? request.getPricePerEmployee() : java.math.BigDecimal.ZERO);
        plan.setReportDownloadPriceUnder50(request.getReportDownloadPriceUnder50() != null ? request.getReportDownloadPriceUnder50() : java.math.BigDecimal.ZERO);
        plan.setReportDownloadPrice50Plus(request.getReportDownloadPrice50Plus() != null ? request.getReportDownloadPrice50Plus() : java.math.BigDecimal.ZERO);

        Plan updatedPlan = planRepository.save(plan);
        log.info("Updated plan: {}", id);
        return convertToResponse(updatedPlan);
    }

    @Transactional
    public void deletePlan(Long id) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found with id: " + id));
        plan.setDeleted(true);
        planRepository.save(plan);
        log.info("Deleted plan: {}", id);
    }

    private PlanResponse convertToResponse(Plan plan) {
        return PlanResponse.builder()
                .id(plan.getId())
                .name(plan.getName())
                .description(plan.getDescription())
                .tier(plan.getTier())
                .planType(plan.getPlanType())
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
                .advancedReports(plan.getAdvancedReports())
                .customLeaveTypes(plan.getCustomLeaveTypes())
                .apiAccess(plan.getApiAccess())
                .prioritySupport(plan.getPrioritySupport())
                .pricePerEmployee(plan.getPricePerEmployee())
                .reportDownloadPriceUnder50(plan.getReportDownloadPriceUnder50())
                .reportDownloadPrice50Plus(plan.getReportDownloadPrice50Plus())
                .build();
    }
}
