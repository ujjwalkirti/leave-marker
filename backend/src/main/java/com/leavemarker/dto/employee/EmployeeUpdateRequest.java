package com.leavemarker.dto.employee;

import com.leavemarker.enums.EmployeeStatus;
import com.leavemarker.enums.EmploymentType;
import com.leavemarker.enums.IndianState;
import com.leavemarker.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EmployeeUpdateRequest {

    @Size(max = 100)
    private String fullName;

    @Email(message = "Invalid email format")
    @Size(max = 100)
    private String email;

    private Role role;

    @Size(max = 100)
    private String department;

    @Size(max = 100)
    private String jobTitle;

    private LocalDate dateOfJoining;

    private EmploymentType employmentType;

    private IndianState workLocation;

    private Long managerId;

    private EmployeeStatus status;
}
