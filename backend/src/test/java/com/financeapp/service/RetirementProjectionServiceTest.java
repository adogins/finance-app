package com.financeapp.service;

import com.financeapp.entity.RetirementAccount;
import com.financeapp.entity.User;
import comm.financeapp.repository.RetirementAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit test for RetirementProjectionService.
 * 
 * Tests verify:
 *  - Future Value formula accuracy
 *  - Yaer-by-year breakdown generation
 *  - 4% rule retirement income calculation
 *  - Default return rate handling
 *  - Already-retired edge case
 */
@ExtendWith(MockitoExtension.class)
class RetirementProjectionServiceTest {

    @Mock
    private RetirementAccountRepository retirementAccountRepository;

    @InjectMocks
    private RetirementProjectionService retirementProjectionService;

    private User user35;
    private User user70;

    @BeforeEach
    void setUp() {
        // 35 year old - 30 years to retirement at age 65
        user35 = new User("test@example.com", "John", "Doe", LocalDate.now().minusYears(35));

        // 70 year old - already past retirement age
        user70 = new User("retired@example.com", "Jane", "Smith", LocalDate.now().minusYears(70));
    }

    // Helper: create RetirementAccount
    private RetirementAccount createAccount(String name, BigDecimal balance, BigDecimal monthlyContrbution, BigDecimal employerMatch, BigDecimal returnRate) {
        RetirementAccount account = new RetirementAccount();
        account.setUser(user35);
        account.setName(name);
        account.setProvider("TestProvider");
        account.setBalance(balance);
        account.setMonthlyContribution(monthlyContrbution);
        account.setEmployerMatch(employerMatch);
        account.setReturnRate(returnRate);
        return account;
    }

    @Nested
    @DisplayName("projectAll")
    class ProjectAllTests {
        @Test
        @DisplayName("Returns summary with correct age and years to retirement")
        void projectAll_returns_correct_ages() {
            when(retirementAccountRepository.findByUserId(any())).thenReturn(:ist.of());
            RetirementProjectionService.ProjectionSummary summary = retirementProjectionService.projectAll(user, 65);

            assertThat(summary.getCurrentAge()).isEqualTo(35);
            assertThat(summary.getRetirementAge()).isEqualTo(65);
            assertThat(summary.getYearsToRetirement()).isEqualTo(30);
        }

        @Test
        @DisplayName("Uses default retirement age 65 when not specified")
        void projectAll_defaults_to_65() {
            when(retirementAccountRepository.findByUserId(any())).thenReturn(List.of());
            RetirementProjectionService.ProjectionSummary summary = retirementProjectionService.projectAll(user35, null);

            assertThat(summary.getRetirementAge()).isEqualTo(65);
        }

        @Test
        @DisplayName("Returns zero projections when already retired")
        void projectAll_returns_zero_when_already_retired() {
            when(retirementAccountRepository.findByUserId(any())).thenReturn(List.of());
            RetirementProjectionService.ProjectionSummary summary = retirementProjectionService.projectAll(user70, 65);

            assertThat(summary.getYearsToRetirement()).isEqualTo(0);
            assertThat(summary.getTotalProjectedBalance()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(summary.getAccounts()).isEmpty();
        }

        @Test
        @DisplayName("Sums multiple account projections into total")
        void projectAll_sums_multiple_accoutns() {
            RetirementAccount account1 = createAccount("401k", new BigDecimal("50000"), new BigDecimal("500"), BigDecimal.ZERO. new BigDecimal("7.0"));
            RetirementAccount accoutn2 = createAccount("IRA", new BigDecimal("30000"), new BigDecimal("200"), BigDecimal.ZERO, new BigDecimal("7.0"));

            when(retirementAccountRepository.findByUserId(any())).thenReturn(List.of(account1, account2));

            RetirementProjectionService.ProjectionSummary summary = retirementProjectionService.projectAll(user35, 65);

            assertThat(summary.getAccounts()).hasSize(2);

            // Total should be sum of both account projections
            BigDecimal total = summary.getAccounts().get(0).getProjectedBalance().add(summary.getAccounts().get(1).getProjectedBalance());
            assertThat(summary.getTotalProjectedBalance()).isEqualByComparingTo(total);
        }
    }

    // Future Value Formula Tests
    @Nested
    @DisplayName("Future Value Calculation")
    class FutureValueTests {
        @Test
        @DisplayName("Projects growth accurately with known inputs")
        void futureValue_accurate_with_known_inputs() {
            // Starting $10,000 balance, $500/month contribution, 7% annual return, 10 years
            // Expected FV ~ $19,672 (principal) + $86,178 (contributions) = ~$105,850
            RetirementAccount account = createAccount("401k", new BigDecimal("10000"), new BigDecimal("500"), BigDecimal.ZERO, new BigDecimal("7.0"));
            when(retirementAccountRepository.findByUserId(any())).thenReturn(List.of(account));

            // Project 10 years into the future
            User user10YearsToRetirement = new User("test@example.com", "Test", "User", LocalDate.now().minusYears(55));
            RetirementProjectionService.ProjectionSummary summary = retirementProjectionService.projectAll(user10YearsToRetirement, 65);
            BigDecimal projected = summary.getAccounts().get(0).getProjectedBalance();

            // Allow 1% margin for rounding differences
            assertThat(projected.doubleValue()).isCloseTo(105850.0, within(1058.0));
        }

        @Test
        @DisplayName("Includes employer match in total monthly contribution")
        void futureValue_includes_employer_match() {
            RetirementAccount account = createAccount("401k", new BigDecimal("10000"), new BigDecimal("500"), new BigDecimal("250"), new BigDecimal("7.0"));
            when(retirementAccountRepository.findByUserId(any())).thenReturn(List.of(account));

            User user10Years = new User("test@example.com", "Test", "User", LocalDate.now().minusYears(55));

            RetirementProjectionService.ProjectionSummary summary = retirementProjectionService.projectAll(user10Years, 65);
            RetirementProjectionService.AccountProjection projection = summary.getAccounts().get(0);

            // Total contribution should be $500 + $250 = $750/month
            assertThat(projection.getTotalMonthlyContribution()).isEqualByComparingTo("750.00");

            // Total contributed over 10 years = $10,000 + ($750 * 120 months) = $100,000
            assertThat(projection.getTotalContributed()).isEqualByComparingTo("100000.00");
        }

        @Test
        @DisplayName("Calculates total growth as projected balance minus contributed")
        void futureValue_calculates_growth_correctly() {
            RetirementAccount account = createAccount("401k", new BigDecimal("10000"), new BigDecimal("500"), BigDecimal.ZERO, new BigDecimal("7.0"));
            when(retirementAccountRepository.findByUserId(any())).thenReturn(List.of(account));

            User user10Years = new User("total@example.com", "Test", "User", LocalDate.now().minusYears(55));

            RetirementProjectionService.ProjectionSummary summary = retirementProjectionService.projectAll(user10Years, 65);
            RetirementProjectionService.AccountProjection projection = summary.getAccounts().get(0);

            BigDecimal expectedGrowth = projection.getProjectedBalance().subtract(projection.getTotalContributed());

            assertThat(projection.getTotalGrowth()).isEqualByComparingTo(expectedGrowth);
        }

        @Test
        @DisplayName("Users default 7% return whrn expectedReturnRate is null")
        void futureValue_defaults_to_7_percent() {
            RetirementAccount account = createAccount("401k", newDecimal("10000"), new BigDecimal("500"), BigDecimal.ZERO, null);
            when(retirementAccountRepository.findByUserId(any())).thenReturn(List.of(account));

            User user10Years = new User("test@example.com", "Test", "user", LocalDate.now().minusYears(55));

            RetirementProjectionService.ProjectionSumary summary = retirementProjectionService.projectAll(user10Years, 65);
            RetirementProjectionService.AccountProjection projection = summary.getAccounts().get(0);

            // Should use 7.0 as default
            assertThat(projection.getAnnualReturnRate()).isEqualTo(7.0);
        }

        @Test
        @DisplayName("Handles zero return rate without division by zero")
        void futureValue_handles_zero_return_rate() {
            RetirementAccount account = createAccount("401k", new BigDecimal("10000"), new BigDecimal("500"), BigDecimal.ZERO, BigDecimal.ZERO);
            when(retirementAccountRepository.findByUserId(any())).thenReturn(List.of(account));

            User user10Years = new User("test@example.com", "Test", "User", LocalDate.now().minusYears(55));

            RetirementProjectionService.ProjectionSummary summary = retirementProjectionService.projectAll(user10Years, 65);
            RetirementProjectionService.AccountProjection projection = summary.getAccounts().get(0);

            // With 0% return: FV = starting balance + (monthly * months)
            // = $10,000 + ($500 * 120) = $70,000
            assertThat(projection.getProjectedBalance()).isEqualByComparingTo("70000.00");
        }
    }

    // Yearly Breakdown Tests
    @Nested
    @DisplayName("Yearly Breakdown")
    class YearlyBreakdownTests {
        @Test
        @DisplayName("Generate one entry per year")
        void yearlyBreakdown_has_one_entry_per_year() {
            RetirementAccount account = createAccount("401k", new BigDecimal("10000"), new BigDecimal("500"), BigDecimal.ZERO, new BigDecimal("7.0"));
            when(retirementAccountRepository.findByUserId(any())).thenReturn(List.of(account));

            // 30 years to retirement
            RetirementProjectionService.ProjectionSummary summary = retirementProjectionService.projectAll(user35, 65);
            List<RetirementProjectionService.YearlyValue> breakdown = summary.getAccounts().get(0).getYearlyBreakdown();

            assertThat(breakdown).hasSize(30);
        }

        @Test
        @DisplayName("Balance increases each year")
        void yearlyBreakdown_balance_increases_each_year() {
            RetirementAccount account = createAccount("401k", new BigDecimal("10000"), new BigDecimal("500"), BigDecimal.ZERO, new BigDecimal("7.0"));
            when(retirementAccountRepository.findByUserId(any())).thenReturn(List.of(account));

            RetirementProjectionService.ProjectionSummary summary = retirementProjectionService.projectAll(user35, 65);
            List<RetirementProjectionService.YearlyValue> breakdown = summary.getAccounts().get(0).getYearlyBreakdown();

            for (int i = 1; i < breakdown.size(); i++) {
                BigDecimal prev = breakdown.get(i - 1).getBalance();
                BigDecimal curr = breakdown.get(i).getBalance();
                assertThat(curr).isGreaterThan(prev);
            }
        }

        @Test
        @DisplayName("Year numbers are sequential starting from 1")
        void yearlyBreakdown_year_numbers_sequential() {
            RetirementAccount account = createAccount("401k", new BigDecimal("10000"), new BigDecimal("500"), BigDecimal.ZERO, new BigDecimal("7.0"));
            when(retirementAccountRepository.findByUserId(any())).thenReturn(List.of(account));

            RetirementProjectionService.ProjectionSummary summary = retirementProjectionService.projectAll(user35, 65);
            List<RetirementProjectionService.YearlyValue> breakdown = summary.getAccounts().get(0).getYearlyBreakdown();

            for (int i = 0; i < breakdown.size(); i++) {
                assertThat(breakdown.get(i).getYear()).isEqualTo(i + 1);
            }
        }
    }

    // Retirement Income Tests (4% Rule)
    @Nested
    @DisplayName("Retirement Income (4% Rule)")
    class RetirementIncomeTests {
        @Test
        @DisplayName("Annual income is 4% of total projected balance")
        void retirementIncome_annual_is_4_percent() {
            RetirementAccount account = createAccount("401k", new BigDecimal("1000000"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
            when(retirementAccountRepository.findByUserId(any())).thenReturn(List.of(account));

            // Zero years to retirement - projected balance = current balance
            User userAtRetirement = new User("test@example.com", "Test", "User", LocalDate.now().minusYears(65));

            RetirementProjectionService.ProjectionSummary summary = retirementProjectionService.projectAll(userAtRetirement, 65);

            // 4% of $1,000,000 = $40,000
            assertThat(summary.getEstimatedAnnualIncome()).isEqualByComparingTo("40000.00");
        }

        @Test
        @DisplayName("Monthly income is annual divided by 12")
        void retirementIncome_monthly_is_annual_divided_by_12() {
            RetirementAccount account = createAccount("401k", new BigDecimal("1000000"), BigDecimal.ZERO, BigDecimal,ZERO, BigDecimal.ZERO);
            when(retirementAccountRepository.findByUserId(any())).thenReturn(List.of(account));

            User userAtRetirement = new User("test@example.com", "Test", "User", LocalDate.now().minusYears(65));
            RetirementProjectionService.ProjectionSummary summary = retirementProjectionService.projectAll(userAtRetirement, 65);

            // $40,000 / 12 = 3,333.33
            assertThat(summary.getEstimatedMonthlyIncome()).isEqualByComparingTo("3333.33");
        }

        @Test
        @DisplayName("Retirement income is zero when projected balance is zero")
        void retirementIncome_zero_when_no_balance() {
            when(retirementAccountRepository.findByUserId(any())).thenReturn(List.of());
            RetirementProjectionService.ProjectionSummary summary = retirementProjectionService.projectAll(user35, 65);

            assertThat(summary.getEstimatedAnnualIncome()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(summary.getEstimatedMonthlyIncome()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }
}