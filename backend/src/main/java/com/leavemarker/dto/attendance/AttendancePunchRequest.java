package com.leavemarker.dto.attendance;

import com.leavemarker.enums.WorkType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class AttendancePunchRequest {

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotNull(message = "Punch time is required")
    private LocalTime punchTime;

    @NotNull(message = "Punch type is required (true for punch-in, false for punch-out)")
    private Boolean isPunchIn;

    private WorkType workType;
}
