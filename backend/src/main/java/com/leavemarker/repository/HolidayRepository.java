package com.leavemarker.repository;

import com.leavemarker.entity.Holiday;
import com.leavemarker.enums.IndianState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Long> {

    List<Holiday> findByCompanyIdAndDeletedFalse(Long companyId);

    List<Holiday> findByCompanyIdAndActiveAndDeletedFalse(Long companyId, Boolean active);

    @Query("SELECT h FROM Holiday h WHERE h.company.id = :companyId AND h.date BETWEEN :startDate AND :endDate AND h.deleted = false")
    List<Holiday> findByCompanyIdAndDateRange(@Param("companyId") Long companyId,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

    @Query("SELECT h FROM Holiday h WHERE h.company.id = :companyId AND (h.state = :state OR h.state IS NULL) AND h.date BETWEEN :startDate AND :endDate AND h.deleted = false")
    List<Holiday> findByCompanyIdAndStateAndDateRange(@Param("companyId") Long companyId,
                                                       @Param("state") IndianState state,
                                                       @Param("startDate") LocalDate startDate,
                                                       @Param("endDate") LocalDate endDate);
}
