package com.leavemarker.dto.leavepolicy;

import com.leavemarker.enums.LeaveType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LeavePolicyResponse {

    private Long id;
    private LeaveType leaveType;
    private Integer annualQuota;
    private Double monthlyAccrual;
    private Boolean carryForward;
    private Integer maxCarryForward;
    private Boolean encashmentAllowed;
    private Boolean halfDayAllowed;
    private Boolean active;
    private Long companyId;
    private String companyName;
}
