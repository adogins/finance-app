package com.financeapp.repository;

import com.financeapp.entity.Expense;
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
 * Repository tests for ExpenseRepository.
 * 
 * Tests custom query methods including category grouping
 */
@DataJpaTest
class ExpenseRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ExpenseRepository expenseRepository;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = new User("user1@example.com", "John", "Doe", LocalDate.of(1990, 1, 1));
        user2 = new User("user2@examples.com", "Jane", "Smith", LocalDate.of(1985, 1, 1));

        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.flush();
    }

    @Test
    @DisplayName("findByUserIdOrderBySpentAtDesc returns expenses for user in descending order")
    void findByUserIdOrderBySpentAtDesc_returns_user_expenses() {
        Expense expense1 = new Expense(user1, new BigDecimal("100"), "Food", "Groceries", LocalDate.of(2025, 3, 1));
        Expense expense2 = new Expense(user1, new BigDecimal("50"), "Food", "Restaurant", LocalDate.of(2025, 3, 15));
        Expense expense3 = new Expense(user2, new BigDecimal("200"), "Housing", "Rent", LocalDate.of(2025, 3, 1));

        entityManager.persist(expense1);
        entityManager.persist(expense2);
        entityManager.persist(expense3);
        entityManager.flush();

        List<Expense> result = expenseRepository.findByUserIdOrderBySpentAtDesc(user1.getId());

        assertThat(result).hasSize(2);

        // March 15 should come before March 1 (desc order)
        assertThat(result.get(0).getSpentAt()).isEqualTo(LocalDate.of(2025, 3, 15));
        assertThat(result.get(1).getSpentAt()).isEqualTo(LocalDate.of(2025, 3, 1));
    }

    @Test
    @DisplayName("findByUserIdAndSpentAtBetween filters by date range")
    void findByUserIdAndSpentAtBetween_filters_by_date() {
        Expense jan = new Expense(user1, new BigDecimal("500"), "Housing", "Jan Rent", LocalDate.of(2025, 1, 1));
        Expense feb = new Expense(user1, new BigDecimal("500"), "Housing", "Feb Rent", LocalDate.of(2025, 2, 1));
        Expense mar = new Expense(user1, new BigDecimal("500"), "Housing", "Mar Rent", LocalDate.of(2025, 3, 1));

        entityManager.persist(jan);
        entityManager.persist(feb);
        entityManager.persist(mar);
        entityManager.flush();

        LocalDate from = LocalDate.of(2025, 2, 1);
        LocalDate to = LocalDate.of(2025, 2, 28);

        List<Expense> result = expenseRepository.findByUserIdAndSpentAtBetween(user1.getId(), from, to);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDescription()).isEqualTo("Feb Rent");
    }

    @Test
    @DisplayName("findByUserIdAndCategory filters expenses by category")
    void findByUserIdAndCategory_filters_correctly() {
        Expense food1 = new Expense(user1, new BigDecimal("50"), "Food", "Groceries", LocalDate.of(2025, 3, 1));
        Expense food2 = new Expense(user1, new BigDecimal("30"), "Food", "Restaurant", LocalDate.of(2025, 3, 5));
        Expense transport = new Expense(user1, new BigDecimal("100"), "Transport", "Gas", LocalDate.of(2025, 3, 10));

        entityManager.persist(food1);
        entityManager.persist(food2);
        entityManager.persist(transport);
        entityManager.flush();

        List<Expense> foodExpenses = expenseRepository.findByUserIdAndCategory(user1.getId(), "Food");

        assertThat(foodExpenses).hasSize(2);
        assertThat(foodExpenses).extracting(Expense::getCategory).containsOnly("Food");
    }

    @Test
    @DisplayName("sumByUserIdAndDateRange calculates total expenses in range")
    void sumByUserIdAndDateRange_sums_correclty() {
        Expense expense1 = new Expense(user1, new BigDecimal("1000"), "Housing", "Rent", LocalDate.of(2025, 3, 1));
        Expense expense2 = new Expense(user1, new BigDecimal("500"), "Food", "Groceries", LocalDate.of(2025, 3, 15));
        Expense expense3 = new Expense(user1, new BigDecimal("300"), "Transport", "Gas", LocalDate.of(2025, 4, 1)); // outside range

        entityManager.persist(expense1);
        entityManager.persist(expense2);
        entityManager.persist(expense3);
        entityManager.flush();

        LocalDate from = LocalDate.of(2025, 3, 1);
        LocalDate to = LocalDate.of(2025, 3, 31);

        BigDecimal total = expenseRepository.sumByUserIdAndDateRange(user1.getId(), from, to);

        assertThat(total).isEqualByComparingTo("1500.00"); // 1000 + 50, excludes April
    }

    @Test
    @DisplaName("sumByCategoryAndDateRange groups expenses by category")
    void sumByCategoryAndDateRange_groups_by_category() {
        Expense food1 = new Expense(user1, new BigDecimal("100"), "Food", "Groceries", LocalDate.of(2025, 3, 5));
        Expense food2 = new Expense(user1, new BigDecimal("50"), "Food", "Restaurant", LocalDate.of(2025, 3, 10));
        Expense housing = new Expense(user1, new BigDecimal("2000"), "Housing", "Rent", LocalDate.of(2025, 3, 1));
        Expense transport = new Expense(user1, new BigDecimal("150"), "Transport", "Gas", LocalDate.of(2025, 3, 15));

        entityManager.persist(food1);
        entityManager.persist(food2);
        entityManager.persist(housing);
        entityManager.persist(transport);
        entityManager.flush();

        LocalDate from = LocalDate.of(2025, 3, 1);
        LocalDate to = LocalDate.of(2025, 3, 31);

        List<Object[]> results = expenseRepository.sumByCategoryAndDateRange(user1.getId(), from, to);

        assertThat(results).hasSize(3);

        // Verify each category sum
        for (Object[] row : results) {
            String category = (String) row[0];
            BigDecimal amount = (BigDecimal) row[1];

            switch (category) {
                case "Food" -> assertThat(amount).isEqualByComparingTo("150.00");
                case "Housing" -> assertThat(amount).isEqualByComparingTo("2000.00");
                case "Transport" -> assertThat(amount).isEqualByComparingTo("150.00");
                default -> throw new AssertionError("Unexpected category: " + category);
            }
        }
    }

    @Test
    @DisplayName("sumByUserIdAndDateRange returns zero when no expenses exist")
    void sumByUserIdAndDateRange_returns_zero_when_empty() {
        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to = LocalDate.of(2025, 12, 31);

        BigDecimal total = expenseRepository.sumByUserIdAndDateRange(user1.getId(), from, to);

        assertThat(total).isEqualByComparingTo("0");
    }
}