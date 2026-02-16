package com.financeapp.repository;

import com.financeapp.entity.RetirementAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface RetirementAccountRepository extends JpaRepository<RetirementAccount, Long> {

    // All retirement accounts for a user
    List<RetirementAccount> findByUserId(Long userId);

    // Sum of all retirement account balances for a user
    @Query("SELECT COALESCE(SUM(r.balance),0) FROM RetirementAccount r WHERE r.user.id = :userId")
    BigDecimal sumBalanceByUserId(@Param("userId") Long userId);

    // Sum of all monthly contributions including employer match
    @Query("SELECT COALESCE(SUM(r.monthlyContribution + COALESCE(r.employerMatch, 0)), 0) FROM RetirementAccount r WHERE r.user.id = :userId")
    BigDecimal sumMonthlyContributionsByUserId(@Param("userId") Long userId);
}