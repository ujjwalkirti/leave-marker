package com.leavemarker.dto.leavepolicy;

import com.leavemarker.enums.LeaveType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LeavePolicyRequest {

    @NotNull(message = "Leave type is required")
    private LeaveType leaveType;

    @NotNull(message = "Annual quota is required")
    @Min(value = 0, message = "Annual quota cannot be negative")
    private Integer annualQuota;

    @NotNull(message = "Monthly accrual is required")
    @Min(value = 0, message = "Monthly accrual cannot be negative")
    private Double monthlyAccrual;

    @NotNull(message = "Carry forward flag is required")
    private Boolean carryForward;

    @Min(value = 0, message = "Max carry forward cannot be negative")
    private Integer maxCarryForward;

    @NotNull(message = "Encashment allowed flag is required")
    private Boolean encashmentAllowed;

    @NotNull(message = "Half day allowed flag is required")
    private Boolean halfDayAllowed;

    @NotNull(message = "Active flag is required")
    private Boolean active;
}
