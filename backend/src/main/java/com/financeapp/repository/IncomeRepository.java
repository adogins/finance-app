package com.financeapp.repository;

import com.financeapp.entity.Income;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface IncomeRepository extends JpaRepository<Income, Long> {

    // All incom entries for a user
    List<Income> findByUserId(Long userId);

    // All income entries for a user ordered by most recent first
    List<Income> findByUserIdOrderByReceivedAtDesc(Long userId);

    // Income entries for a user within a date range
    List<Income> findByUserIdAndReceivedAtBetween(Long userId, LocalDate startDate, LocalDate endDate);

    // Sum of all income for a user within a date range
    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Income i WHERE i.user.id = :userId AND i.receivedAt BETWEEN :from AND :to")
    BigDecimal sumByUserIdAndDateRange(@Param("userId") Long userId, @Param("from") LocalDate from,
            @Param("to") LocalDate to);
}