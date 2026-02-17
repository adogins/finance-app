package com.financeapp.service;

import com.financeapp.entity.User;
import com.financeapp.repository.AssetRepository;
import com.financeapp.repository.ExpenseRepository;
import com.financeapp.repository.IncomeRepository;
import com.financeapp.repository.LiabilityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit tests for FinanceRatioService.
 * 
 * All respository calls are mocked = no database required.
 * Tests verify ratio values, status thresholds, and age bracket benchmarks.
 */
@ExtendWith(MockitoExtension.class)
class FinanceRatioServiceTest {

    @Mock private IncomeRepository incomeRepository;
    @Mock private ExpenseRepository expenseRepository;
    @Mock private AssetRepository assetRepository;
    @Mock private LiabilityRepository liabilityRepository;

    @InjectMocks private FinanceRatioService financeRatioService;

    // Shared test user
    private User user30s;
    private User user20s;
    private User user50s;

    @BeforeEach
    void setUp() {
        // 35 year old -> "30s" bracket
        user30s = new User("test@example.com", "Alice", "Doe", LocalDate.now().minusYears(35));

        // 25 year old -> "20s" bracket
        user20s = new User("young@example.com", "Bob", "Doe", LocalDate.now().minusYears(25));

        // 55 year old -> "50s" bracket
        user50s = new User("older@example.com", "Charlie", "Smith", LocalDate.now().minusYears(55));
    }

    // Helper: set common mock data returns
    private void mockRepositories(BigDecimal income, BigDecimal expenses, BigDecimal totalAssets, BigDecimal liquidAssets, BigDecimal totalLiabilities, BigDecimal monthlyDebt) {
        when (incomeRepository.sumByUserIdAndDataRange(any(), any(), any())).thenReturn(income);
        when(expenseRepository.sumByUserIdAndDataRange(any(), any(), any())).thenReturn(expenses);
        when(assetRepository.sumBalanceByUserId(any())).thenReturn(totalAssets);
        when(assetRepository.sumLiquidAssetsByUserId(any())).thenReturn(liquidAssets);
        when(liabilityRepository.sumBalanceByUserId(any())).thenReturn(totalLiabilities);
        when(liabilityRepository.sumMonthlyPaymentsByUserId(any())).thenReturn(monthlyDebt);
    }

    // Savings Rate Tests
    @Nested
    @DisplayName("Savings Rate")
    class SavingsRateTests {
        @Test
        @DisplayName("GOOD status when savings rate exceeds bracket benchmark")
        void savingsRate_good_when_above_benchmark() {
            // 30s benchmark max is 20% - set rate to 25%
            mockRepositories(
                new BigDecimal("8000"), // income
                new BigDecimal("6000"), // expenses -> saves $2000 = 25%
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
            );

            Map<String FinanceRatioService.RatioResult> results = financeRatioService.calculateAll(user30s);

            FinanceRatioService.RatioResult ratio = result.get("savingsRate");
            assertThat(ratio.getStatus()).isEqualTo("GOOD");
            assertThat(ratio.getValue()).isEqualByComparingTo("25.00");
            assertThat(ratio.getAgeBracket()).isEqualTo("30s");
        }

        @Test
        @DisplayName("WARNING status when savings rate is between min and max benchmark")
        void savingsRate_warning_when_between_benchmarks() {
            // 30s benchmark: min=15%, max=20% - set rate to 17%
            mockRepositories(
                new BigDecimal("8000"), // income
                new BigDecimal("6640"), // expenses -> saves $1360 = 17%
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
            );

            Map<String, FinanceRatioService.RatioResult> results = financeRatioService.calculateAll(user30s);

            assertThat(results.get("savingsRate").getStatus()).isEqualTo("WARNING");
        }

        @Test
        @DisplayName("CRITICAL status when savings rate is below minimum benchmark")
        void savingsRate_critical_when_below_minimum() {
            // 30s benchmark min is 15% - set rate to 5%
            mockRepositories(
                new BigDecimal("8000"), // income
                new BigDecimal("7600"), // expenses -> saves $400 = 5%
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
            );

            Map<String, FinanceRatioService.RatioResult> results = financeRatioService.calculateAll(user30s);

            assertThat(results.get("savingsRate").getStatus()).isEqualTo("CRITICAL");
        }

        @Test
        @DisplayName("NO_DATA when income is zero")
        void savingsRate_noData_when_income_is_zero() {
            mockRepositories(
                    BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
            );
            Map<String, FinanceRatioService.RatioResult> results = financeRatioService.calculateAll(user30s);
            assertThat(results.get("savingsRate").getStatus()).isEqualTo("NO_DATA");
        }

        @Test
        @DisplayName("20s bracket has lower benchmark than 50s bracket")
        void savingsRate_benchmarks_increase_with_age() {
            // Same 20% savings rate should be GOOD for 20s, WARNING for 50s
            mockRepositories(
                    new BigDecimal("5000"), // income
                    new BigDecimal("4000"), // expenses -> saves $1000 = 20%
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
            );

            Map<String, FinanceRatioService.RatioResult> results20s = financeRatioService.calculateAll(user20s);
            Map<String, FinanceRatioService.RatioResult> results50s = financeRatioService.calculateAll(user50s);

            assertThat(results20s.get("savingsRate").getStatus()).isEqualTo("GOOD");
            assertThat(results50s.get("savingsRate").getStatus()).isEqualTo("WARNING");
        }
    }

    // Debt-to-Income Tests
    @Nested
    @DisplayName("Debt-to-Income")
    class DebtToIncomeTests {
        @Test
        @DisplayName("GOOD status when debt payments are well below 36%")
        void debtToIncome_good_when_well_below_benchmark() {
            // Monthly debt = $1000, income = $5000 -> 20% - below 36% * 0.75 = 27%
            mockRepositories(
                    new BigDecimal("5000"), new BigDecimal("3000"), 
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    new BigDecimal("1000")
            );

            Map<String, FinanceRatioService.RatioResult> results = financeRatioService.calculateAll(user30s);

            FinanceRatioService.RatioResult ratio = results.get("debtToIncome");
            assertThat(ratio.getStatus()).isEqualTo("GOOD");
            assertThat(ratio.getValue()).isEqualByComparingTo("20.00");
        }

        @Test
        @DisplayName("WARNING status when debt payments are between 27% and 36%")
        void debtToIncome_warning_when_near_benchmarks() {
            // Monthly debt = $1600, income = $5000 -> 32% 
            mockRepositories(
                    new BigDecimal("5000"), new BigDecimal("3000"), 
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    new BigDecimal("1600")
            );

            assertThat(financeRatioService.calculateAll(user30s).get("debtToIncome").getStatus()).isEqualTo("WARNING");
        }

        @Test
        @DisplayName("CRITICAL status when debt exceeds 36%")
        void debtToIncome_critical_when_above_benchmark() {
            // Monthly debt = $2000, income = $5000 -> 40%
            mockRepositories(
                    new BigDecimal("5000"), new BigDecimal("3000"), 
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    new BigDecimal("2000")
            );

            assertThat(financeRatioService.calculateAll(user30s).get("debtToIncome").getStatus()).isEqualTo("CRITICAL");
        }

        @Test
        @DisplayName("Benchmark is fixed at 36% regardless of age")
        void debtToIncome_same_benchmark_all_ages() {
            // 40% debt-to-income should be crititcal for all age brackets
            mockRepositories(
                    new BigDecimal("5000"), BigDecimal.ZERO, 
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    new BigDecimal("2000")
            );

            String status20s = financeRatioService.calculateAll(user20s).get("debtToIncome").getStatus();
            String status30s = financeRatioService.calculateAll(user30s).get("debtToIncome").getStatus();
            String status50s = financeRatioService.calculateAll(user50s).get("debtToIncome").getStatus();

            assertThat(status20s).isEqualTo("CRITICAL");
            assertThat(status30s).isEqualTo("CRITICAL");
            assertThat(status50s).isEqualTo("CRITICAL");
        }

        @Test
        @DisplayName("Benchmark max value is 36.0")
        void debtToIncome_benchmark_max_is_36() {
            mockRepositories(
                    new BigDecimal("5000"), BigDecimal.ZERO, 
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    new BigDecimal("500")
            );

            FinanceRatioService.RatioResult ratio = financeRatioService.caluculateAll(user30s).get("debtToIncome");
            assertThat(ratio.getBenchmarkMax()).isEqualTo(36.0);
        }
    }

    // Emergency Fund Tests
    @Nested
    @DisplayName("Emergency Fund")
    class EmergencyFundTests {
        @Test
        @DisplayName("GOOD when liquid assets conver benchmark months of expenses")
        void emergencyFund_good_when_sufficient() {
            // 30s benchmark max = 6 monthg - liquid = $24000, expenses = $3000 -> 8 months
            mockRepositories(
                    new BigDecimal("5000"),
                    new BigDecimal("3000"),
                    new BigDecimal("50000"), 
                    new BigDecimal("24000"), // liquid assets
                    BigDecimal.ZERO, BigDecimal.ZERO
            );

            assertThat(financeRatioService.calculateAll(user30s).get("emergencyFund").getStatus()).isEqualTo("GOOD");
        }

        @Test
        @DisplayName("CRITICAL when liquid assets cover less than minimum benchmark")
        void emergencyFund_critical_when_insufficient() {
            // 30s benchmark min = 3 months - liquid = $3000, expenses = $3000 -> 1 month
            mockRepositories(
                    new BigDecimal("5000"),
                    new BigDecimal("3000"),
                    new BigDecimal("50000"), 
                    new BigDecimal("3000"), 
                    BigDecimal.ZERO, BigDecimal.ZERO
            );

            assertThat(financeRatioService.calculateAll(user30s).get("emergencyFund").getStatus()).isEqualTo("CRITICAL");
        }

        @Test
        @DisplayName("NO_DATA when expenses are zero")
        void emergencyFund_noData_when_no_expense() {
            mockRepositories(
                    new BigDecimal("5000"), BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
            );

            assertThat(financeRatioService.calculateAll(user30s).get("emergencyFund").getStatus()).isEqualTo("NO_DATA");
        }
    }

    // Net Worth Ratio Tests
    @Nested
    @DisplayName("Net Worth Ratio")
    class NewWorthRatioTests {
        @Test
        @DisplayName("GOOD when net worth meets age/10 x income target")
        void netWorthRatio_good_when_on_track() {
            // user30s is 35 -> target = 35/10 = 3.5x annual income
            // income = $8000/month = $96000/year -> target net worth = $336000
            // assets = $400000, liabilities = $50000 -> net worth = $350000 > $336000
            mockRepositories(
                    new BigDecimal("8000"), new BigDecimal("5000"), 
                    new BigDecimal("400000"), new BigDecimal("50000"), 
                    new BigDecimal("50000"), BigDecimal.ZERO
            );

            assertThat(financeRatioService.calculateAll(user30s).get("netWorthRatio").getStatus()).isEqualTo("GOOD");
        }

        @Test
        @DisplayName("CRITICAL when net worth is below target")
        void netWorthRatio_critical_when_far_behind() {
            // user30s target = 3.5x - set net worth well below 50% of target
            mockRepositories(
                    new BigDecimal("8000"), new BigDecimal("5000"), 
                    new BigDecimal("20000"), BigDecimal.ZERO, 
                    new BigDecimal("5000"), BigDecimal.ZERO
            );

            assertThat(financeRatioService.calculateAll(user30s).get("netWorthRatio").getStatus()).isEqualTo("CRITICAL");
        }
    }

    // Debt-to-Asset Tests
    @Nested
    @DisplayName("Debt-to-Asset Ratio")
    class DebtToAssetTests {
        @Test
        @DisplayName("GOOD when liabilities are well below benchmark percentage of assets")
        void debtToAsser_good_when_low() {
            // 30s benchmark max = 60% = set ratio to 20%
            // assets = $500000, liabilities = $100000 -> 20%
            mockRepositories(
                    new BigDecimal("5000"), new BigDecimal("3000"), 
                    new BigDecimal("500000"), BigDecimal.ZERO, 
                    new BigDecimal("100000"), BigDecimal.ZERO
            );

            assertThat(financeRatioService.calculateAll(user30s).get("debtToAsset").getStatus()).isEqualTo("GOOD");
        }

        @Test
        @DisplayName("WARNING when approaching benchmark")
        void debtToAsset_warning_when_near_benchmakr() {
            // 30s benchmark = 60% - set ratio to 55%
            mockRepositories(
                    new BigDecimal("5000"), new BigDecimal("3000"), 
                    new BigDecimal("200000"), BigDecimal.ZERO, 
                    new BigDecimal("110000"), BigDecimal.ZERO
            );

            assertThat(financeRatioService.calculateAll(user30s).get("debtToAsset").getStatus()).isEqualTo("WARNING");
        }

        @Test
        @DisplayName("CRITICAL when exceeding benchmark")
        void debtToAsset_critical_when_high() {
            // 30s benchmark = 60% - set to ratio 80%
            mockRepositories(
                    new BigDecimal("5000"), new BigDecimal("3000"), 
                    new BigDecimal("100000"), BigDecimal.ZERO, 
                    new BigDecimal("80000"), BigDecimal.ZERO
            );

            assertThat(financeRatioService.calculateAll(user30s).get("debtToAsset").getStatus()).isEqualTo("CRITICAL");
        }

        @Test
        @DisplayName("Benchmarks decrease with age")
        void debtToAsset_benchmarks_decrease_with_age() {
            // 50% ratio: should be WARNING for 20s (75% max), CRITUCAL for 50s (25% max)
            mockRepositories(
                    new BigDecimal("5000"), new BigDecimal("3000"), 
                    new BigDecimal("100000"), BigDecimal.ZERO, 
                    new BigDecimal("50000"), BigDecimal.ZERO
            );

            String status20s = financeRatioService.calculateAll(user20s).get("debtToAsset").getStatus();
            String status50s = financeRatioService.calculateAll(user50s).get("debtToAsset").getStatus();

            assertThat(status20s).isEqualTo("WARNING");
            assertThat(status50s).isEqualTo("CRITICAL");
        }

        @Test
        @DisplayName("NO_DATA when assets are zero")
        void debtToAsset_noData_when_no_assets() {
            mockRepositories(
                    new BigDecimal("5000"), new BigDecimal("3000"), 
                    BigDecimal.ZERO, BigDecimal.ZERO, 
                    new BigDecimal("10000"), BigDecimal.ZERO
            );

            assertThat(financeRatioService.calculateAll(user30s).get("debtToAsset").getStatus()).isEqualTo("NO_DATA");
        }
    }

    // All Ratio Present
    @Nested
    @DisplayName("calculateAll")
    class CalculateAllTests {
        @Test
        @DisplayName("Returns all 6 ratio keys")
        void calculateAll_returns_all_six_ratios() {
            mockRepositories(
                    new BigDecimal("5000"), new BigDecimal("3000"),
                    new BigDecimal("100000"), new BigDecimal("20000"),
                    new BigDecimal("30000"), new BigDecimal("500")
            );

            Map<String, FinanceRatioService.RatioResult> results = financeRatioService.calculateAll(user30s);

            asserThat(results).containsKeys(
                "savingsRate", 
                "debtToIncome",
                "emergencyFund",
                "netWorthRatio",
                "liquidAssetRatio",
                "debtToAsset"
            );
        }

        @Test
        DisplayName("All ratio return NO_DATA when income and expenses are zero")
        void calculateAll_noData_when_no_financial_activity() {
            mockRepositories(
                    BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO
            );

            Map<String, FinanceRatioService.RatioResult> results = financeRatioService.calculateAll(user30s);

            assertThat(results.get("savingsRate").getStatus()).isEqualTo("NO_DATA");
            assertThat(results.get("debtToIncome").getStatus()).isEqualTo("NO_DATA");
            assertThat(results.get("emergencyFund").getStatus()).isEqualTo("NO_DATA");
            assertThat(results.get("netWorthRatio").getStatus()).isEqualTo("NO_DATA");
            assertThat(results.get("liquidAssetRatio").getStatus()).isEqualTo("NO_DATA");
            assertThat(results.get("debtToAsset").getStatus()).isEqualTo("NO_DATA");
        }
    }
}