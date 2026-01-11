package com.leavemarker.dto.holiday;

import com.leavemarker.enums.HolidayType;
import com.leavemarker.enums.IndianState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HolidayResponse {

    private Long id;
    private String name;
    private LocalDate date;
    private HolidayType type;
    private IndianState state;
    private Boolean active;
    private Long companyId;
    private String companyName;
}
