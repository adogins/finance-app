package com.financeapp.repository;

import com.financeapp.entity.Liability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface LiabilityRepository extends JpaRepository<Liability, Long> {
    
    // All liabilities for a user
    List<Liability> findByUserId(Long userId);

    // Liabilities by type
    List<Liability> findByUserIdAndType(Long userId, String type);

    // SUm of all liability balances for a user
    @Query("SELECT COALESCE(SUM(l.balance), 0) FROM Liability l WHERE l.user.id = :userId")
    BigDecimal sumBalanceByUserId(@Param("userId") Long userId);

    // Sum of all monthly payments for a user
    // Only includes liabilities where monthly_payment has been set
    @Query("SELECT COALESCE(SUM(l.monthlyPayment), 0) FROM Liability l WHERE l.user.id = :userId AND l.monthlyPayment IS NOT NULL")
    BigDecimal sumMonthlyPaymentsByUserId(@Param("userId") Long userId);