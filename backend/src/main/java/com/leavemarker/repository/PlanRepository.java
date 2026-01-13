package com.leavemarker.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.leavemarker.entity.Plan;
import com.leavemarker.enums.PlanTier;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {
    List<Plan> findByActiveTrue();
    List<Plan> findByTierAndActiveTrue(PlanTier tier);
    // Optional<Plan> findByTierAndActiveTrue(PlanTier tier, Boolean active);
    List<Plan> findByTier(PlanTier tier);
}
