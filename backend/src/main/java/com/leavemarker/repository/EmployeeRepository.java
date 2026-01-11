package com.leavemarker.repository;

import com.leavemarker.entity.Employee;
import com.leavemarker.enums.EmployeeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmailAndDeletedFalse(String email);

    Optional<Employee> findByIdAndDeletedFalse(Long id);

    Optional<Employee> findByPasswordResetTokenAndDeletedFalse(String token);

    List<Employee> findByCompanyIdAndDeletedFalse(Long companyId);

    List<Employee> findByCompanyIdAndStatusAndDeletedFalse(Long companyId, EmployeeStatus status);

    List<Employee> findByManagerIdAndDeletedFalse(Long managerId);

    boolean existsByCompanyIdAndEmailAndDeletedFalse(Long companyId, String email);

    boolean existsByCompanyIdAndEmployeeIdAndDeletedFalse(Long companyId, String employeeId);

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.company.id = :companyId AND e.deleted = false")
    long countByCompanyId(@Param("companyId") Long companyId);
}
