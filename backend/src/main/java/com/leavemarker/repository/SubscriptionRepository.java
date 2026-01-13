package com.leavemarker.repository;

import com.leavemarker.entity.Company;
import com.leavemarker.entity.Subscription;
import com.leavemarker.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByCompanyAndStatus(Company company, SubscriptionStatus status);
    List<Subscription> findByCompany(Company company);
    List<Subscription> findByEndDateBeforeAndStatus(LocalDateTime endDate, SubscriptionStatus status);
    Optional<Subscription> findFirstByCompanyOrderByEndDateDesc(Company company);
}
