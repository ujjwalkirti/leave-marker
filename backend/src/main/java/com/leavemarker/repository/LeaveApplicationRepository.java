package com.leavemarker.repository;

import com.leavemarker.entity.LeaveApplication;
import com.leavemarker.enums.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveApplicationRepository extends JpaRepository<LeaveApplication, Long> {

    List<LeaveApplication> findByEmployeeIdAndDeletedFalse(Long employeeId);

    List<LeaveApplication> findByEmployeeIdAndStatusAndDeletedFalse(Long employeeId, LeaveStatus status);

    @Query("SELECT la FROM LeaveApplication la WHERE la.employee.manager.id = :managerId AND la.status = :status AND la.deleted = false")
    List<LeaveApplication> findByManagerIdAndStatus(@Param("managerId") Long managerId, @Param("status") LeaveStatus status);

    @Query("SELECT la FROM LeaveApplication la WHERE la.employee.company.id = :companyId AND la.status = :status AND la.deleted = false")
    List<LeaveApplication> findByCompanyIdAndStatus(@Param("companyId") Long companyId, @Param("status") LeaveStatus status);

    @Query("SELECT la FROM LeaveApplication la WHERE la.employee.id = :employeeId AND " +
           "((la.startDate BETWEEN :startDate AND :endDate) OR (la.endDate BETWEEN :startDate AND :endDate)) AND " +
           "la.status IN ('PENDING', 'APPROVED') AND la.deleted = false")
    List<LeaveApplication> findOverlappingLeaves(@Param("employeeId") Long employeeId,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);

    @Query("SELECT la FROM LeaveApplication la WHERE la.employee.company.id = :companyId AND " +
           "la.startDate >= :startDate AND la.endDate <= :endDate AND la.deleted = false")
    List<LeaveApplication> findByCompanyIdAndDateRange(@Param("companyId") Long companyId,
                                                        @Param("startDate") LocalDate startDate,
                                                        @Param("endDate") LocalDate endDate);
}
