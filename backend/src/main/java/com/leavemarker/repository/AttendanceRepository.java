package com.leavemarker.repository;

import com.leavemarker.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByEmployeeIdAndDateAndDeletedFalse(Long employeeId, LocalDate date);

    List<Attendance> findByEmployeeIdAndDeletedFalse(Long employeeId);

    @Query("SELECT a FROM Attendance a WHERE a.employee.id = :employeeId AND a.date BETWEEN :startDate AND :endDate AND a.deleted = false")
    List<Attendance> findByEmployeeIdAndDateRange(@Param("employeeId") Long employeeId,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);

    @Query("SELECT a FROM Attendance a WHERE a.employee.company.id = :companyId AND a.date BETWEEN :startDate AND :endDate AND a.deleted = false")
    List<Attendance> findByCompanyIdAndDateRange(@Param("companyId") Long companyId,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);

    @Query("SELECT a FROM Attendance a WHERE a.correctionRequested = true AND a.correctionApproved = false AND a.employee.company.id = :companyId AND a.deleted = false")
    List<Attendance> findPendingCorrections(@Param("companyId") Long companyId);
}
