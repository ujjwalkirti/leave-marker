package com.leavemarker.dto.holiday;

import com.leavemarker.enums.HolidayType;
import com.leavemarker.enums.IndianState;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class HolidayRequest {

    @NotBlank(message = "Holiday name is required")
    @Size(max = 200, message = "Holiday name must not exceed 200 characters")
    private String name;

    @NotNull(message = "Holiday date is required")
    private LocalDate date;

    @NotNull(message = "Holiday type is required")
    private HolidayType type;

    private IndianState state;

    @NotNull(message = "Active flag is required")
    private Boolean active;
}
