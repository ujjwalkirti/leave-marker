package com.leavemarker.dto.attendance;

import com.leavemarker.enums.WorkType;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalTime;

@Data
public class AttendanceCorrectionRequest {

    private LocalTime punchInTime;

    private LocalTime punchOutTime;

    private WorkType workType;

    @Size(max = 500, message = "Remarks must not exceed 500 characters")
    private String remarks;
}
