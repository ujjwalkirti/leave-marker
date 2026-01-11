package com.leavemarker.dto.attendance;

import com.leavemarker.enums.AttendanceStatus;
import com.leavemarker.enums.WorkType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceResponse {

    private Long id;
    private Long employeeId;
    private String employeeName;
    private String employeeEmail;
    private LocalDate date;
    private LocalTime punchInTime;
    private LocalTime punchOutTime;
    private WorkType workType;
    private AttendanceStatus status;
    private String remarks;
    private Boolean correctionRequested;
    private Boolean correctionApproved;
}
