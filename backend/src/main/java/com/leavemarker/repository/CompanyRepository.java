package com.leavemarker.repository;

import com.leavemarker.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByEmailAndDeletedFalse(String email);

    boolean existsByEmailAndDeletedFalse(String email);
}
