package com.financeapp.repository;

import com.financeapp.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    
    // All expenses for a user ordered by most recent to least recent
    List<Expense> findByUserIdOrderBySpentAtDesc(Long userId);

    // Expenses for a user within a date range
    List<Expense> findByUserIdAndSpentAtBetweenOrderBySpentAtDesc(Long userId, LocalDate from, LocalDate to);

    // Expenses for a user by category
    List<Expense> findByUserIdAndCategory(Long userId, String category);

    // Sum of all expenses for a user within a date range
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user.id = :userId AND e.spentAT BETWEEN :from AND :to")
    BigDecimal sumByUserIdAndDateRange(@Param("userId") Long userId,
                                       @Param("from") LocalDate from,
                                       @Param("to") LocalDate to);

    // Sum of expenses grouped by category for a date range
    @Query("SELECT e.category, COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user.id = :userId AND e.spentAt BETWEEN :from AND :to GROUP BY e.category")
    List<Object[]> sumByCategoryAndDateRange(@Param("userId") Long userId,
                                             @Param("from") LocalDate from,
                                             @Param("to") LocalDate to);
} 