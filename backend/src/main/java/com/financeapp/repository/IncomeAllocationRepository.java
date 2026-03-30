package com.financeapp.repository;

import com.financeapp.entity.IncomeAllocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncomeAllocationRepository extends JpaRepository<IncomeAllocation, Long> {

    // All income allocations for a user ordered by priority (lowest number =
    // highest priority)
    List<IncomeAllocation> findByUserIdOrderByPriorityAsc(Long userId);

    // Income allocations by type
    List<IncomeAllocation> findByUserIdAndAllocationType(Long userId, IncomeAllocation.AllocationType allocationType);

    // Check if a category already has an allocation for the user
    boolean existsByUserIdAndCategory(Long userId, String category);
}