package com.financeapp.repository;

import com.financeapp.entity.Liability;
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
 * Repository tests for LiabilityRepository.
 * 
 * Tests custom query methods including monthly payment summation.
 */
@DataJpaTest
class LiabilityRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private LiabilityRepository liabilityRepository;

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
    @DisplayName("findByUserId returns only liabilities for specified user")
    void findByUserId_returns_only_user_liabilities() {
        Liability mortgage = new Liability(user1, "Home Loan", "Mortgage", new BigDecimal("300000"), new BigDecimal("3.5"), new BigDecimal("1500"));
        Liability carLoan = new Liability(user1, "Car Loan", "Auto", new BigDecimal("25000"), new BigDecimal("5.0"), new BigDecimal("450"));
        Liability otherDebt = new Liability(user2, "Credit Card", "Credit Card", new BigDecimal("5000"), new BigDecimal("18.0"), new BigDecimal("150"));

        entityManager.persist(mortgage);
        entityManager.persist(carLoan);
        entityManager.persist(otherDebt);
        entityManager.flush();

        List<Liability> result = liabilityRepository.findByUserId(user1.getId());

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Liability::getName).containsExactlyInAnyOrder("Home Loan", "Car Loan");
    }

    @Test
    @DisplayName("findByUserIdAndType filters by type")
    void findByUserIdAndType_filters_correctly() {
        Liability mortgage = new Liability(user1, "Primary Home", "Mortgage", new BigDecimal("300000"), new BigDecimal("3.5"), new BigDecimal("1500"));
        Liability secondHome = new Liability(user1, "Vacation Home", "Mortgage", new BigDecimal("200000"), new BigDecimal("4.0"), new BigDecimal("1000"));
        Liability carLoan = new Liability(user1, "Car Loan", "Auto", new BigDecimal("25000"), new BigDecimal("5.0"), new BigDecimal("450"));

        entityManager.persist(mortgage);
        entityManager.persist(secondHome);
        entityManager.persist(carLoan);
        entityManager.flush();

        List<Liability> mortgages = liabilityRepository.findByUserIdAndType(user1.getId(), "Mortgage");

        assertThat(mortgages).hasSize(2);
        assertThat(mortgages).extracting(Liability::getType).containsOnly("Mortgage");
    }

    @Test
    @DisplayName("sumBalanceByUserId calculates total liability balance")
    void sumBalanceByUserId_sums_all_liabilities() {
        Liability mortgage = new Liability(user1, "Home Loan", "Mortgage", new BigDecimal("300000"), new BigDecimal("3.5"), new BigDecimal("1500"));
        Liability carLoan = new Liability(user1, "Car Loan", "Auto", new BigDecimal("25000"), new BigDecimal("5.0"), new BigDecimal("450"));
        Liability creditCard = new Liability(user1, "Credit Card", "Credit Card", new BigDecimal("5000"), new BigDecimal("18.0"), new BigDecimal("150"));

        entityManager.persist(mortgage);
        entityManager.persist(carLoan);
        entityManager.persist(creditCard);
        entityManager.flush();

        BigDecimal total = liabilityRepository.sumBalanceByUserId(user1.getId());

        assertThat(total).isEqualByComparingTo(new BigDecimal("330000"));

    }

    @Test
    @DisplayName("sumMonthlyPaymentByUserId calculates total monthly debt payments")
    void sumMonthlyPaymentByUserId_sums_monthly_payments() {
        Liability mortgage = new Liability(user1, "Home Loan", "Mortgage", new BigDecimal("300000"), new BigDecimal("3.5"), new BigDecimal("1500"));
        Liability carLoan = new Liability(user1, "Car Loan", "Auto", new BigDecimal("25000"), new BigDecimal("5.0"), new BigDecimal("450"));
        Liability creditCard = new Liability(user1, "Credit Card", "Credit Card", new BigDecimal("5000"), new BigDecimal("18.0"), new BigDecimal("150"));

        entityManager.persist(mortgage);
        entityManager.persist(carLoan);
        entityManager.persist(creditCard);
        entityManager.flush();

        BigDecimal totalMonthlyPayment = liabilityRepository.sumMonthlyPaymentByUserId(user1.getId());

        assertThat(totalMonthlyPayment).isEqualByComparingTo(new BigDecimal("2100")); // 1500 + 450 + 150 = 2100
    }

    @Test
    @DisplayName("sumMonthlyPaymentsByUserId excludes liabilities without monthly payment set")
    void sumMonthlyPaymentsByUserId_excludes_null_payments() {
        Liability withPayment = new Liability(user1, "Car Loan", "Auto", new BigDecimal("25000"), new BigDecimal("5.0"), new BigDecimal("450"));
        Liability withoutPayment = new Liability(user1, "Student Loan", "Student", new BigDecimal("50000"), new BigDecimal("4.0"), null); // no monthly payment set

        entityManager.persist(withPayment);
        entityManager.persist(withoutPayment);
        entityManager.flush();

        BigDecimal totalMonthly = liabilityRepository.sumMonthlyPaymentByUserId(user1.getId());

        assertThat(totalMonthly).isEqualByComparingTo(new BigDecimal("450.00")); // should only include car loan payment
    }

    @Test
    @DisplayName("sumBalanceByUserUserId returns zero wehn no liabilities exist")
    void sumBalanceByUserId_returns_zero_when_empty() {
        BigDecimal total = liabilityRepository.sumBalanceByUserId(user1.getId());

        assertThat(total).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("sumMonthlyPaymentsByUserId returns zero when no monthly payments exist")
    void sumMonthlyPaymentsByUserId_returns_zero_when_empty() {
        BigDecimal total = liabilityRepository.sumMonthlyPaymentByUserId(user1.getId());

        assertThat(total).isEqualByComparingTo("0");
    }
}