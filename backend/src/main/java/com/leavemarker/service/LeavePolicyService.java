package com.leavemarker.service;

import com.leavemarker.dto.leavepolicy.LeavePolicyRequest;
import com.leavemarker.dto.leavepolicy.LeavePolicyResponse;
import com.leavemarker.entity.Company;
import com.leavemarker.entity.LeavePolicy;
import com.leavemarker.exception.BadRequestException;
import com.leavemarker.exception.ResourceNotFoundException;
import com.leavemarker.repository.CompanyRepository;
import com.leavemarker.repository.LeavePolicyRepository;
import com.leavemarker.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeavePolicyService {

    private final LeavePolicyRepository leavePolicyRepository;
    private final CompanyRepository companyRepository;
    private final SubscriptionFeatureService subscriptionFeatureService;

    @Transactional
    public LeavePolicyResponse createLeavePolicy(LeavePolicyRequest request, UserPrincipal currentUser) {
        Company company = companyRepository.findById(currentUser.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        // Check subscription plan limits
        subscriptionFeatureService.validateCanAddLeavePolicy(company.getId());

        if (leavePolicyRepository.existsByCompanyIdAndLeaveTypeAndDeletedFalse(
                company.getId(), request.getLeaveType())) {
            throw new BadRequestException("Leave policy for this leave type already exists");
        }

        if (request.getCarryForward() && (request.getMaxCarryForward() == null || request.getMaxCarryForward() < 0)) {
            throw new BadRequestException("Max carry forward must be specified when carry forward is enabled");
        }

        LeavePolicy policy = LeavePolicy.builder()
                .company(company)
                .leaveType(request.getLeaveType())
                .annualQuota(request.getAnnualQuota())
                .monthlyAccrual(request.getMonthlyAccrual())
                .carryForward(request.getCarryForward())
                .maxCarryForward(request.getMaxCarryForward() != null ? request.getMaxCarryForward() : 0)
                .encashmentAllowed(request.getEncashmentAllowed())
                .halfDayAllowed(request.getHalfDayAllowed())
                .active(request.getActive())
                .build();

        policy = leavePolicyRepository.save(policy);
        return mapToResponse(policy);
    }

    public LeavePolicyResponse getLeavePolicy(Long id, UserPrincipal currentUser) {
        LeavePolicy policy = leavePolicyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave policy not found"));

        if (!policy.getCompany().getId().equals(currentUser.getCompanyId())) {
            throw new BadRequestException("Access denied");
        }

        return mapToResponse(policy);
    }

    public List<LeavePolicyResponse> getAllLeavePolicies(UserPrincipal currentUser) {
        List<LeavePolicy> policies = leavePolicyRepository.findByCompanyIdAndDeletedFalse(
                currentUser.getCompanyId());
        return policies.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<LeavePolicyResponse> getActiveLeavePolicies(UserPrincipal currentUser) {
        List<LeavePolicy> policies = leavePolicyRepository.findByCompanyIdAndActiveAndDeletedFalse(
                currentUser.getCompanyId(), true);
        return policies.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public LeavePolicyResponse updateLeavePolicy(Long id, LeavePolicyRequest request, UserPrincipal currentUser) {
        LeavePolicy policy = leavePolicyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave policy not found"));

        if (!policy.getCompany().getId().equals(currentUser.getCompanyId())) {
            throw new BadRequestException("Access denied");
        }

        if (!policy.getLeaveType().equals(request.getLeaveType())) {
            if (leavePolicyRepository.existsByCompanyIdAndLeaveTypeAndDeletedFalse(
                    currentUser.getCompanyId(), request.getLeaveType())) {
                throw new BadRequestException("Leave policy for this leave type already exists");
            }
        }

        if (request.getCarryForward() && (request.getMaxCarryForward() == null || request.getMaxCarryForward() < 0)) {
            throw new BadRequestException("Max carry forward must be specified when carry forward is enabled");
        }

        policy.setLeaveType(request.getLeaveType());
        policy.setAnnualQuota(request.getAnnualQuota());
        policy.setMonthlyAccrual(request.getMonthlyAccrual());
        policy.setCarryForward(request.getCarryForward());
        policy.setMaxCarryForward(request.getMaxCarryForward() != null ? request.getMaxCarryForward() : 0);
        policy.setEncashmentAllowed(request.getEncashmentAllowed());
        policy.setHalfDayAllowed(request.getHalfDayAllowed());
        policy.setActive(request.getActive());

        policy = leavePolicyRepository.save(policy);
        return mapToResponse(policy);
    }

    @Transactional
    public void deleteLeavePolicy(Long id, UserPrincipal currentUser) {
        LeavePolicy policy = leavePolicyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave policy not found"));

        if (!policy.getCompany().getId().equals(currentUser.getCompanyId())) {
            throw new BadRequestException("Access denied");
        }

        policy.setDeleted(true);
        leavePolicyRepository.save(policy);
    }

    private LeavePolicyResponse mapToResponse(LeavePolicy policy) {
        return LeavePolicyResponse.builder()
                .id(policy.getId())
                .leaveType(policy.getLeaveType())
                .annualQuota(policy.getAnnualQuota())
                .monthlyAccrual(policy.getMonthlyAccrual())
                .carryForward(policy.getCarryForward())
                .maxCarryForward(policy.getMaxCarryForward())
                .encashmentAllowed(policy.getEncashmentAllowed())
                .halfDayAllowed(policy.getHalfDayAllowed())
                .active(policy.getActive())
                .companyId(policy.getCompany().getId())
                .companyName(policy.getCompany().getName())
                .build();
    }
}
