package com.leavemarker.service;

import com.leavemarker.dto.employee.EmployeeRequest;
import com.leavemarker.dto.employee.EmployeeResponse;
import com.leavemarker.dto.employee.EmployeeUpdateRequest;
import com.leavemarker.entity.Company;
import com.leavemarker.entity.Employee;
import com.leavemarker.enums.EmployeeStatus;
import com.leavemarker.exception.BadRequestException;
import com.leavemarker.exception.ResourceNotFoundException;
import com.leavemarker.repository.CompanyRepository;
import com.leavemarker.repository.EmployeeRepository;
import com.leavemarker.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final SubscriptionFeatureService subscriptionFeatureService;

    @Transactional
    public EmployeeResponse createEmployee(EmployeeRequest request, UserPrincipal currentUser) {
        Company company = companyRepository.findById(currentUser.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        // Check subscription plan limits
        subscriptionFeatureService.validateCanAddEmployee(company.getId());

        if (employeeRepository.existsByCompanyIdAndEmailAndDeletedFalse(company.getId(), request.getEmail())) {
            throw new BadRequestException("Employee with this email already exists");
        }

        if (employeeRepository.existsByCompanyIdAndEmployeeIdAndDeletedFalse(company.getId(), request.getEmployeeId())) {
            throw new BadRequestException("Employee ID already exists");
        }

        Employee manager = null;
        if (request.getManagerId() != null) {
            manager = employeeRepository.findByIdAndDeletedFalse(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));
        }

        Employee employee = Employee.builder()
                .company(company)
                .employeeId(request.getEmployeeId())
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .department(request.getDepartment())
                .jobTitle(request.getJobTitle())
                .dateOfJoining(request.getDateOfJoining())
                .employmentType(request.getEmploymentType())
                .workLocation(request.getWorkLocation())
                .manager(manager)
                .status(EmployeeStatus.ACTIVE)
                .build();

        employee = employeeRepository.save(employee);
        return mapToResponse(employee);
    }

    public EmployeeResponse getEmployee(Long id, UserPrincipal currentUser) {
        Employee employee = employeeRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        if (!employee.getCompany().getId().equals(currentUser.getCompanyId())) {
            throw new BadRequestException("Access denied");
        }

        return mapToResponse(employee);
    }

    public List<EmployeeResponse> getAllEmployees(UserPrincipal currentUser) {
        List<Employee> employees = employeeRepository.findByCompanyIdAndDeletedFalse(currentUser.getCompanyId());
        return employees.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<EmployeeResponse> getActiveEmployees(UserPrincipal currentUser) {
        List<Employee> employees = employeeRepository.findByCompanyIdAndStatusAndDeletedFalse(
                currentUser.getCompanyId(), EmployeeStatus.ACTIVE);
        return employees.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public EmployeeResponse updateEmployee(Long id, EmployeeUpdateRequest request, UserPrincipal currentUser) {
        Employee employee = employeeRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        if (!employee.getCompany().getId().equals(currentUser.getCompanyId())) {
            throw new BadRequestException("Access denied");
        }

        if (request.getFullName() != null) {
            employee.setFullName(request.getFullName());
        }
        if (request.getEmail() != null && !request.getEmail().equals(employee.getEmail())) {
            if (employeeRepository.existsByCompanyIdAndEmailAndDeletedFalse(
                    currentUser.getCompanyId(), request.getEmail())) {
                throw new BadRequestException("Employee with this email already exists");
            }
            employee.setEmail(request.getEmail());
        }
        if (request.getRole() != null) {
            employee.setRole(request.getRole());
        }
        if (request.getDepartment() != null) {
            employee.setDepartment(request.getDepartment());
        }
        if (request.getJobTitle() != null) {
            employee.setJobTitle(request.getJobTitle());
        }
        if (request.getDateOfJoining() != null) {
            employee.setDateOfJoining(request.getDateOfJoining());
        }
        if (request.getEmploymentType() != null) {
            employee.setEmploymentType(request.getEmploymentType());
        }
        if (request.getWorkLocation() != null) {
            employee.setWorkLocation(request.getWorkLocation());
        }
        if (request.getManagerId() != null) {
            Employee manager = employeeRepository.findByIdAndDeletedFalse(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));
            employee.setManager(manager);
        }
        if (request.getStatus() != null) {
            employee.setStatus(request.getStatus());
        }

        employee = employeeRepository.save(employee);
        return mapToResponse(employee);
    }

    @Transactional
    public void deactivateEmployee(Long id, UserPrincipal currentUser) {
        Employee employee = employeeRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        if (!employee.getCompany().getId().equals(currentUser.getCompanyId())) {
            throw new BadRequestException("Access denied");
        }

        employee.setDeleted(true);
        employeeRepository.save(employee);
    }

    private EmployeeResponse mapToResponse(Employee employee) {
        return EmployeeResponse.builder()
                .id(employee.getId())
                .employeeId(employee.getEmployeeId())
                .fullName(employee.getFullName())
                .email(employee.getEmail())
                .role(employee.getRole())
                .department(employee.getDepartment())
                .jobTitle(employee.getJobTitle())
                .dateOfJoining(employee.getDateOfJoining())
                .employmentType(employee.getEmploymentType())
                .workLocation(employee.getWorkLocation())
                .status(employee.getStatus())
                .managerId(employee.getManager() != null ? employee.getManager().getId() : null)
                .managerName(employee.getManager() != null ? employee.getManager().getFullName() : null)
                .companyId(employee.getCompany().getId())
                .companyName(employee.getCompany().getName())
                .build();
    }
}
