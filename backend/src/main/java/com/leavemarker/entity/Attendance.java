package com.leavemarker.entity;

import com.leavemarker.enums.AttendanceStatus;
import com.leavemarker.enums.WorkType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "attendance",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"employee_id", "date"})
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private LocalDate date;

    @Column
    private LocalTime punchInTime;

    @Column
    private LocalTime punchOutTime;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private WorkType workType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttendanceStatus status;

    @Column(length = 500)
    private String remarks;

    @Column(nullable = false)
    @Builder.Default
    private Boolean correctionRequested = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean correctionApproved = false;
}
