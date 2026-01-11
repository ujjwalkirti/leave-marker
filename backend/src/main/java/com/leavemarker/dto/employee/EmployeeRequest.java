package com.leavemarker.dto.employee;

import com.leavemarker.enums.EmploymentType;
import com.leavemarker.enums.IndianState;
import com.leavemarker.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EmployeeRequest {

    @NotBlank(message = "Employee ID is required")
    @Size(max = 50)
    private String employeeId;

    @NotBlank(message = "Full name is required")
    @Size(max = 100)
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotNull(message = "Role is required")
    private Role role;

    @Size(max = 100)
    private String department;

    @Size(max = 100)
    private String jobTitle;

    @NotNull(message = "Date of joining is required")
    private LocalDate dateOfJoining;

    @NotNull(message = "Employment type is required")
    private EmploymentType employmentType;

    @NotNull(message = "Work location is required")
    private IndianState workLocation;

    private Long managerId;
}
