package com.financeapp.repository;

import com.financeapp.entity.Income;
import com.financeapp.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository tests for IncomeRepository.
 * 
 * Uses @DataJpaTest with an in-memory H2 database
 * Tests custon query methods
 */

@DataJpaTest
class IncomeRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private IncomeRepository incomeRepository;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = new User("user1@example.com", "John", "Doe", LocalDate.of(1990, 1, 1));
        user2 = new User("user2@example.com", "Jane", "Smith", LocalDate.of(1985, 1, 1));

        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.flush();
    }

    @Test
    @DisplayName("findByUserId returns only income for specified user")
    void findByUserId_returns_only_user_income() {
        Income income1 = new Income(user1, new BigDecimal("5000"), "Salary", LocalDate.of(2025, 3, 1));
        Income income2 = new Income(user1, new BigDecimal("500"), "Bonus", LocalDate.of(2025, 3, 15));
        Income income3 = new Income(user2, new BigDecimal("3000"), "Freelance", LocalDate.of(2025, 3, 1));

        entityManager.persist(income1);
        entityManager.persist(income2);
        entityManager.persist(income3);
        entityManager.flush();

        List<Income> result = incomeRepository.findByUserIdOrderByReceivedAtDesc(user1.getId());

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Income::getAmount).containsExaclty(new BigDecimal("500.00"), new BigDecimal("5000.00")) // desc order
    }

    @Test
    @DisplayName("findByUserIdAndReceivedAtBetween filters by date range")
    void findByUserIdAndReceivedAtBetween_filters_by_date() {
        Income jan = new Income(user1, new BigDecimal("5000"), "Jan Salary", LocalDate.of(2025, 1, 15));
        Income feb = new Income(user1, new BigDecimal("5000"), "Feb Salary", LocalDate.of(2025, 2, 15));
        Income mar = new Income(user1, new BigDecimal("5000"), "Mar Salary", LocalDate.of(2025, 3, 15));

        entityManager.persist(jan);
        entityManager.persist(feb);
        entityManager.persist(mar);
        entityManager.flush();

        LocalDate from = LocalDate.of(2025, 2, 1);
        LocalDate to = LocalDate.of(2025, 2, 28);

        List<Income> result = incomeRepository.findByUserIdAndReceivedAtBetween(user1.getId(), from, to);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSource()).isEqualTo("Feb Salary");
    }

    @Test
    @DisplayName("sumByUserIdAndDateRage calculates total income in range")
    void sumByUserIdAndDateRange_sums_correctly() {
        Income income1 = new Income(user1, new BigDecimal("3000"), "Part 1", LocalDate.of(2025, 3, 5));
        Income income2 = new Income(user1, new BigDecimal("2000"), "Part 2", LocalDate.of(2025, 3, 15));
        Income income3 = new Income(user1, new BigDecimal("1000"), "Part 3", LocalDate.of(2025, 4, 5));  // outside range

        entityManager.persist(income1);
        entityManager.persist(income2);
        entityManager.persist(income3);
        entityManager.flush();

        LocalDate from = LocalDate.of(2025, 3, 1);
        LocalDate to = LocalDate.of(2025, 3, 31);

        BigDecimal total = incomeRepository.sumByUserIdAndReceivedAtBetween(user1.getId(), from, to);

        assertThat(total).isEqualByComparingTo("5000.00"); // 3000 + 2000, excludes April");
    }

    @Test
    @DisplayName("sumByUserIdAndDateRange returns zero when no income exists")
    void sumByUserIdAndDateRange_returns_zero_when_empty() {
        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to = LocalDate.of(2025, 12, 31);

        BigDecimal total = incomeRepository.sumByUserIdAndReceivedAtBetween(user1.getId(), from, to);

        assertThat(total).isEqualByComparingTo("0");
    }
}