package com.leavemarker.dto.leaveapplication;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LeaveApprovalRequest {

    @NotNull(message = "Approval status is required")
    private Boolean approved;

    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason;
}
