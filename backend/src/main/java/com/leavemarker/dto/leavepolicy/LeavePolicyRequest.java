package com.leavemarker.dto.leavepolicy;

import com.leavemarker.enums.LeaveType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LeavePolicyRequest {

    @NotNull(message = "Leave type is required")
    private LeaveType leaveType;

    @NotNull(message = "Annual quota is required")
    @Min(value = 0, message = "Annual quota cannot be negative")
    @Max(value = 365, message = "Annual quota cannot exceed 365 days")
    private Integer annualQuota;

    @NotNull(message = "Monthly accrual is required")
    @Min(value = 0, message = "Monthly accrual cannot be negative")
    @Max(value = 31, message = "Monthly accrual cannot exceed 31 days")
    private Double monthlyAccrual;

    @NotNull(message = "Carry forward flag is required")
    private Boolean carryForward;

    @Min(value = 0, message = "Max carry forward cannot be negative")
    @Max(value = 365, message = "Max carry forward cannot exceed 365 days")
    private Integer maxCarryForward;

    @NotNull(message = "Encashment allowed flag is required")
    private Boolean encashmentAllowed;

    @NotNull(message = "Half day allowed flag is required")
    private Boolean halfDayAllowed;

    @NotNull(message = "Active flag is required")
    private Boolean active;
}
