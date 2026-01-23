package com.leavemarker.service;

import com.leavemarker.dto.auth.*;
import com.leavemarker.entity.Company;
import com.leavemarker.entity.Employee;
import com.leavemarker.entity.Plan;
import com.leavemarker.entity.Subscription;
import com.leavemarker.enums.BillingCycle;
import com.leavemarker.enums.EmployeeStatus;
import com.leavemarker.enums.EmploymentType;
import com.leavemarker.enums.PlanTier;
import com.leavemarker.enums.Role;
import com.leavemarker.enums.SubscriptionStatus;
import com.leavemarker.exception.BadRequestException;
import com.leavemarker.exception.ResourceNotFoundException;
import com.leavemarker.repository.CompanyRepository;
import com.leavemarker.repository.EmployeeRepository;
import com.leavemarker.repository.PlanRepository;
import com.leavemarker.repository.SubscriptionRepository;
import com.leavemarker.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;
    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public JwtAuthResponse signup(SignupRequest request) {
        if (companyRepository.existsByEmailAndDeletedFalse(request.getCompanyEmail())) {
            throw new BadRequestException("Company email already exists");
        }

        if (employeeRepository.existsByCompanyIdAndEmailAndDeletedFalse(null, request.getEmail())) {
            throw new BadRequestException("Employee email already exists");
        }

        Company company = Company.builder()
                .name(request.getCompanyName())
                .email(request.getCompanyEmail())
                .timezone("Asia/Kolkata")
                .active(true)
                .build();
        company = companyRepository.save(company);

        Employee employee = Employee.builder()
                .company(company)
                .employeeId(request.getEmployeeId())
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.SUPER_ADMIN)
                .dateOfJoining(LocalDate.now())
                .employmentType(EmploymentType.FULL_TIME)
                .workLocation(request.getWorkLocation())
                .status(EmployeeStatus.ACTIVE)
                .build();
        employee = employeeRepository.save(employee);

        // Auto-create FREE subscription for new companies
        createFreeSubscription(company);

        String jwt = tokenProvider.generateTokenFromEmail(
                employee.getEmail(),
                employee.getId(),
                employee.getRole().name(),
                company.getId()
        );

        return JwtAuthResponse.builder()
                .accessToken(jwt)
                .userId(employee.getId())
                .email(employee.getEmail())
                .fullName(employee.getFullName())
                .role(employee.getRole().name())
                .companyId(company.getId())
                .build();
    }

    public JwtAuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        Employee employee = employeeRepository.findByEmailAndDeletedFalse(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        return JwtAuthResponse.builder()
                .accessToken(jwt)
                .userId(employee.getId())
                .email(employee.getEmail())
                .fullName(employee.getFullName())
                .role(employee.getRole().name())
                .companyId(employee.getCompany().getId())
                .build();
    }

    @Transactional
    public void requestPasswordReset(PasswordResetRequest request) {
        Employee employee = employeeRepository.findByEmailAndDeletedFalse(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with email: " + request.getEmail()));

        String resetToken = UUID.randomUUID().toString();
        employee.setPasswordResetToken(resetToken);
        employee.setPasswordResetTokenExpiry(LocalDate.now().plusDays(1));
        employeeRepository.save(employee);

        // TODO: Send email with reset token
        // For now, we'll just log it
        System.out.println("Password reset token: " + resetToken);
    }

    @Transactional
    public void resetPassword(PasswordResetConfirmRequest request) {
        Employee employee = employeeRepository.findByPasswordResetTokenAndDeletedFalse(request.getToken())
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));

        if (employee.getPasswordResetTokenExpiry().isBefore(LocalDate.now())) {
            throw new BadRequestException("Reset token has expired");
        }

        employee.setPassword(passwordEncoder.encode(request.getNewPassword()));
        employee.setPasswordResetToken(null);
        employee.setPasswordResetTokenExpiry(null);
        employeeRepository.save(employee);
    }

    /**
     * Creates a FREE subscription for a newly registered company.
     * The FREE plan allows up to 10 employees with basic features.
     */
    private void createFreeSubscription(Company company) {
        Plan freePlan = planRepository.findFirstByTierAndActiveTrueAndDeletedFalse(PlanTier.FREE)
                .orElse(null);

        if (freePlan == null) {
            log.warn("No active FREE plan found. Company {} will not have a subscription.", company.getId());
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        // FREE plan never expires - set to far future
        LocalDateTime farFuture = now.plusYears(100);

        Subscription subscription = Subscription.builder()
                .company(company)
                .plan(freePlan)
                .status(SubscriptionStatus.ACTIVE)
                .billingCycle(BillingCycle.MONTHLY)
                .startDate(now)
                .endDate(farFuture)
                .currentPeriodStart(now)
                .currentPeriodEnd(farFuture)
                .amount(BigDecimal.ZERO)
                .autoRenew(false)
                .isPaid(true) // FREE plan is always "paid"
                .build();

        subscriptionRepository.save(subscription);
        log.info("Created FREE subscription for company: {}", company.getName());
    }
}
