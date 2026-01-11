package com.leavemarker.entity;

import com.leavemarker.enums.HolidayType;
import com.leavemarker.enums.IndianState;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "holidays")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Holiday extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private HolidayType type;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private IndianState state;

    @Column(nullable = false)
    private Boolean active = true;
}
