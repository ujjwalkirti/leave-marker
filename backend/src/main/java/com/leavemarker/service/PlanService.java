package com.leavemarker.service;

import com.leavemarker.dto.plan.PlanRequest;
import com.leavemarker.dto.plan.PlanResponse;
import com.leavemarker.entity.Plan;
import com.leavemarker.enums.PlanTier;
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
                .billingCycle(request.getBillingCycle())
                .monthlyPrice(request.getMonthlyPrice())
                .yearlyPrice(request.getYearlyPrice())
                .minEmployees(request.getMinEmployees() != null ? request.getMinEmployees() : 1)
                .maxEmployees(request.getMaxEmployees() != null ? request.getMaxEmployees() : 10)
                .maxLeavePolicies(request.getMaxLeavePolicies() != null ? request.getMaxLeavePolicies() : 3)
                .active(request.getActive() != null ? request.getActive() : true)
                .attendanceTracking(request.getAttendanceTracking() != null ? request.getAttendanceTracking() : false)
                .advancedReports(request.getAdvancedReports() != null ? request.getAdvancedReports() : false)
                .customLeaveTypes(request.getCustomLeaveTypes() != null ? request.getCustomLeaveTypes() : false)
                .apiAccess(request.getApiAccess() != null ? request.getApiAccess() : false)
                .prioritySupport(request.getPrioritySupport() != null ? request.getPrioritySupport() : false)
                .attendanceRateAnalytics(request.getAttendanceRateAnalytics() != null ? request.getAttendanceRateAnalytics() : false)
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
        plan.setBillingCycle(request.getBillingCycle());
        plan.setMonthlyPrice(request.getMonthlyPrice());
        plan.setYearlyPrice(request.getYearlyPrice());
        plan.setMinEmployees(request.getMinEmployees() != null ? request.getMinEmployees() : 1);
        plan.setMaxEmployees(request.getMaxEmployees() != null ? request.getMaxEmployees() : 10);
        plan.setMaxLeavePolicies(request.getMaxLeavePolicies() != null ? request.getMaxLeavePolicies() : 3);
        plan.setActive(request.getActive() != null ? request.getActive() : true);
        plan.setAttendanceTracking(request.getAttendanceTracking() != null ? request.getAttendanceTracking() : false);
        plan.setAdvancedReports(request.getAdvancedReports() != null ? request.getAdvancedReports() : false);
        plan.setCustomLeaveTypes(request.getCustomLeaveTypes() != null ? request.getCustomLeaveTypes() : false);
        plan.setApiAccess(request.getApiAccess() != null ? request.getApiAccess() : false);
        plan.setPrioritySupport(request.getPrioritySupport() != null ? request.getPrioritySupport() : false);
        plan.setAttendanceRateAnalytics(request.getAttendanceRateAnalytics() != null ? request.getAttendanceRateAnalytics() : false);

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
                .billingCycle(plan.getBillingCycle())
                .monthlyPrice(plan.getMonthlyPrice())
                .yearlyPrice(plan.getYearlyPrice())
                .minEmployees(plan.getMinEmployees())
                .maxEmployees(plan.getMaxEmployees())
                .maxLeavePolicies(plan.getMaxLeavePolicies())
                .active(plan.getActive())
                .attendanceTracking(plan.getAttendanceTracking())
                .advancedReports(plan.getAdvancedReports())
                .customLeaveTypes(plan.getCustomLeaveTypes())
                .apiAccess(plan.getApiAccess())
                .prioritySupport(plan.getPrioritySupport())
                .attendanceRateAnalytics(plan.getAttendanceRateAnalytics())
                .build();
    }
}
