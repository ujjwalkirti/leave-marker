package com.leavemarker.dto.leaveapplication;

import com.leavemarker.enums.LeaveType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class LeaveApplicationRequest {

    @NotNull(message = "Leave type is required")
    private LeaveType leaveType;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotNull(message = "Half day flag is required")
    private Boolean isHalfDay;

    @Size(max = 1000, message = "Reason must not exceed 1000 characters")
    private String reason;

    @Size(max = 500, message = "Attachment URL must not exceed 500 characters")
    private String attachmentUrl;
}
