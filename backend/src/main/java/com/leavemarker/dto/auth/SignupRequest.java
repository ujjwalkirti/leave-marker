package com.leavemarker.dto.auth;

import com.leavemarker.enums.IndianState;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignupRequest {

    @NotBlank(message = "Company name is required")
    @Size(max = 200, message = "Company name cannot exceed 200 characters")
    private String companyName;

    @NotBlank(message = "Company email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Company email cannot exceed 100 characters")
    private String companyEmail;

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name cannot exceed 100 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "Employee ID is required")
    @Size(max = 50, message = "Employee ID cannot exceed 50 characters")
    private String employeeId;

    @NotNull(message = "Work location is required")
    private IndianState workLocation;
}
