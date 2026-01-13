package com.leavemarker.dto.leavebalance;

import com.leavemarker.enums.LeaveType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveBalanceResponse {
    private Long id;
    private LeaveType leaveType;
    private Integer year;
    private Double totalQuota;
    private Double used;
    private Double pending;
    private Double available;
    private Double carriedForward;
}
