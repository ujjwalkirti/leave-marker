package com.leavemarker.dto.employee;

import com.leavemarker.enums.EmployeeStatus;
import com.leavemarker.enums.EmploymentType;
import com.leavemarker.enums.IndianState;
import com.leavemarker.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeResponse {

    private Long id;
    private String employeeId;
    private String fullName;
    private String email;
    private Role role;
    private String department;
    private String jobTitle;
    private LocalDate dateOfJoining;
    private EmploymentType employmentType;
    private IndianState workLocation;
    private EmployeeStatus status;
    private Long managerId;
    private String managerName;
    private Long companyId;
    private String companyName;
}
