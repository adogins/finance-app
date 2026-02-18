package com.financeapp.service;

import com.financeapp.entity.Snapshot;
import com.financeapp.entity.User;
import com.financeapp.repository.ExpenseRepository;
import com.financeapp.repository.IncomeRepository;
import com.financeapp.repository.SnapshotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.apt.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit tests for BalanceSheetService.
 * 
 * Tests verify:
 *  - Monthly income/expense aggregation
 *  - Savings rate calculation
 *  - Category breakdown grouping
 *  - Opening/closing net worth from snapshots
 *  - Yearly aggregation across 12 months
 *  - Null handling when snapshots don't exist
 */
@ExtendWith(MockitoExtension.class)
class BalanceSheetServiceTests {
    @Mock private IncomeRepository incomeRepository;
    @Mock private ExpenseRepository expenseRepository;
    @Mock private SnapshotRepository snapshotRepository;

    @InjectMocks private BalanceSheetService balanceSheetService;

    private static final Long USER_ID = 1L;

    // Monthly Balance Sheet Tests
    @Nested
    @DisplayName("Monthly Balance Sheet")
    class MonthlyBalanceSheetTests {
        @Test
        @DisplayName("Monthly Balance Sheet")
        void monthly_calculates_net_cash_flow() {
            when(incomeRepository.sumByUserIdAndDateRange(any(), any(), any())).thenReturn(new BigDecimal("8000"));
            when(expenseRepository.sumByUserIdAndDateRange(any(), any(), any())).thenReturn(new BigDecimal("5000"));
            when(snapshotRepository.sumByCategoryAndDateRange(any(), any(), any())).thenReturn(List.of());

            BalanceSheetService.MonthlyBalanceSheet sheet = balanceSheetService.getMonthly(USER_ID, 2025, 3);

            assertThat(sheet.getTotalIncome()).isEqualByComparingTo("8000.00");
            assertThat(sheet.getTotalExpenses()).isEqualByComparingTo("5000.00");
            assertThat(sheet.getNetCashFlow()).isEqualByComparingTo("3000.00");
        }

        @Test
        @DisplayName("Calculates savings rate as net cash flow divided by income")
        void monthly_calculates_savings_rate() {
            when(incomeRepository.sumByUserIdAndDateRange(any(), any(), any())).thenReturn(new BigDecimal("8000"));
            when(expenseRepository.sumByUserIdAndDateRange(any(), any(), any())).thenReturn(new BigDecimal("6000"));
            when(expenseRepository.sumByCategoryAndDateRange(any(), any(), any())).thenReturn(List.of());

            BalanceSheetService.MonthlyBalanceSheet sheet = balanceSheetService.getMonthly(USER_ID, 2025, 3);

            // Net = $2000, INcome = $8000 -> 25%
            assertThat(sheet.getSavingsRate()).isEqualByComparingTo("25.00");
        }

        @Test
        @DisplayName("Savings rate is zero when income is zero")
        void monthly_savingsRate_zero_when_no_income() {
            when(incomeRepository.sumByUserIdAndDateRange(any(), any(), any())).thenReturn(BigDecimal.ZERO);
            when(expenseRepository.sumByUserIdAndDateRange(any(), any(), any())).thenReturn(new BigDecimal("500"));
            when(expenseRepository.sumByCategoryAndDateRange(any(), any(), any())).thenReturn(List.of());

            BalanceSheetService.MonthlyBalanceSheet sheet = balanceSheetService.getMonthly(USER_ID, 2025, 3);

            assertThat(sheet.getSavingsRate()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Returns expense breakdown grouped by category")
        void monthly_returns_expense_breakdown_by_category() {
            when(incomeRepository.sumByUserIdAndDateRange(any(), any(), any())).thenReturn(new BigDecimal("8000"));
            when(expenseRepository.sumByUserIdAndDateRange(any(), any(), any())).thenReturn(new BigDecimal("5000"));
            when(expenseRepository.sumByCategoryAndDateRange(any(), any(), any()))
                    .thenReturn(List.of(   
                            new Object[]{"Housing", new BigDecimal("2000")},
                            new Object[]{"Food", new BigDecimal("800")},
                            new Object[]{"Transportation", new BigDecimal("600")}
                    ));

            BalanceSheetService.MonthlyBalanceSheet sheet = balanceSheetService.getMonthly(USER_ID, 2025, 3);

            assertThat(sheet.getExpensesByCategory()).hasSize(3);
            assertThat(sheet.getExpensesByCategory().get(0).getCategory()).isEqualTo("Housing");
            assertThat(sheet.getExpensesByCategory().get(0).getAmount()).isEqualByComparingTo("2000.00");
        }

        @Test
        @DisplayName("Includes opening networth from nearest snapshot on or before month start")
        void monthly_includes_opening_net_worth() {
            when(incomeRepository.sumByUserIdAndDateRange(any(), any(), any())).thenReturn(new BigDecimal("8000"));
            when(expenseRepository.sumByUserIdAndDateRange(any(), any(), any())).thenReturn(new BigDecimal("5000"));
            when(expenseRpository.sumByCategoryAndDateRange(any(), any(), any())).thenReturn(List.of());

            Snapshot openingSnapshot = new Snapshot();
            openingSnapshot.setNetWorth(new BigDecimal("145000"));

            when(snapshot.Repository.findTopByUserIdAndSanpshotDateLessThanEqualOrderBySnapshotDateDesc(
                    eq(USER_ID), any()))
                    .thenReturn(Optional.of(openingSnapshot));

            BalanceSheetService.MonthlyBalanceSheet sheet = balanceSheetService.getMonthly(USER_ID, 2025, 3);
            
            assertThat(sheet.getOpeningNetWorth()).isEqualByComparingTo("145000");
        }

        @Test
        @DisplayName("Opening net worth is null when no snapshot exists")
        void monthly_opening_net_worth_null_when_no_snapshot() {
            when(incomeRepository.sumByUserIdAndDateRange(any(), any(), any())).thenReturn(new BigDecimal("8000"));
            when(expenseRepository.sumByUserIdAndDateRange(any(), any(), any())).thenReturn(new BigDecimal("5000"));
            when(expenseRepository.sumByCategoryAndDateRange(any(), any(), any())).thenReturn(list.of());
            when(snapshotRepository.findTopByUserIdAndSnapshotDateLessThanEqualOrderBySnapshotDateDesc(
                    eq(USER_ID), any()))
                    .thenReturn(Optional.empty());

            BalanceSheetService.MonthlyBalanceSheet sheet = balanceSheetService.getMonthly(USER_ID, 2025, 3);

            assertThat(sheet.getOpeningNetWorth()).isNull();
            assertThat(sheet.getClosingNetWorth()).isNull();
        }

        @Test
        @DisplayName("Period label includes full month name and year")
        void monthly_period_label_formatted() {
            when(incomeRepository.sumByUserIdAndDateRange(any(), any(), any())).thenReturn(BigDecimal.ZERO);
            when(expenseRepository.sumByUserIdAndDateRange(any(), any(), any())).thenReturn(BigDecimal.ZERO);
            when(expenseRepository.sumByCategoryAndDateRange(any(), any(), any())).thenReturn(List.of());

            BalanceSheetService.MonthlyBalanceSheet sheet = balanceSheetService.getMonthly(USER_ID, 2025, 3);

            assertThat(sheet.getPeriod()).isEqualTo("2025-03");
            assertThat(sheet.getPeriodLabel()).isEqualTo("March 2025");
        }
    }

    // Yearlt Balance Sheet Tests
    @Nested
    @DisplayName("Yearly Balance Sheet")
    class YearlyBalanceSheetTests {
        @BeforeEach
        void setUp() {
            // Mock monthly data
            when (incomeRepository.sumByUserIdAndDateRange(any(), any(), any())).thenReturn(new BigDecimal("5000"));
            when(expenseRepository.sumByUserIdAndDateRange(any(), any(), any())).thenReturn(new BigDecimal("3000"));
            when(expenseRepository.sumByCategoryAndDateRange(any(), any(), any())).thenReturn(List.of());
        }

        @Test
        @DisplayName("Returns 12 months monthly balance sheets")
        void yearly_returns_12_months() {
            BalanceSheetService.YearlyBalanceSheet sheet = balanceSheetService.getYearly(USER_ID, 2025);
            assertThat(sheet.getMonths()).hasSize(12);
        }

        @Test
        @DisplayName("Annual income is sum of all 12 months")
        void yearly_annual_income_is_sum_of_months() {
            BalanceSheetService.YearlyBalanceSheet sheet = balanceSheetService.getYearly(USER_ID, 2025);
           
            // Each month = $5000 * 12 months = $60,000
            assertThat(sheet.getAnnualIncome()).isEqualByComparingTo("60000.00");
        }

        @Test
        @DisplayName("Annual expenses is sume of all 12 months")
        void yearly_annual_expenses_is_sum_of_months() {
            BalanceSheetService.YearlyBalanceSheet sheet = balanceSheetService.getYearly(USER_ID, 2025);

            // Each month = $3000 * 12 onths = $36,000
            assertThat(sheet.getAnnualExpenses()).isEqualByComparingTo("36000.00");
        }

        @Test
        @DisplayName("Annual net cash flow is income minus expenses")
        void yearly_annual_net_cash_flow() {
            BalanceSheetService.YearlyBalanceSheet sheet = balanceSheetService.getYearly(USER_ID, 2025);

            // $60,000 = $36,000 = $24,000
            assertThat(sheet.getAnnualNetCashFlow()).isEqualByComparingTo("24000.00");
        }

        @Test
        @DisplayName("Annual savings rate is net cash flow divided by annual income")
        void yearly_annual_savings_rate() {
            BalanceSheetService.YearlyBalanceSheet sheet = balanceSheetService.getYearly(USER_ID, 2025);

            // $24,000 / $60,000 = 40%
            assertThat(sheet.getAnnualSavingsRate()).isEqualByComparingTo("40.00");
        }

        @Test
        @DisplayName("Includes opening net worth from Jan 1 snapshot")
        void yearly_includes_opening_net_worth() {
            Snapshot janSnapshot = new Snapshot();
            janSnapshot.setNetWorth(new BigDecimal("100000"));

            when(snapshotRepository.findTopByUserIdAndSnapshotDateLessThanEqualOrderBySnapshotDateDesc(
                    eq(USER_ID), eq(LocalDate.of(2025, 1, 1))))
                    .thenReturn(Optional.of(janSnapshot));

            BalanceSheetService.YearlyBalanceSheet sheet = balanceSheetService.getYearly(USER_ID, 2025);

            assertThat(sheet.getOpeningNetWorth()).isEqualByComparingTo("100000");
        }

        @Test
        @DisplayName("Includes closing net worth from Dec 31 snapshot")
        void yearly_includes_closing_net_worth() {
            Snapshot decSnapshot = new Snapshot();
            decSnapshot.setNetWorth(new BigDecimal("150000"));

            when(snapshotRepository.findTopByUserIdAndSnapshotDateLessThanEqualOrderBySnapshotDateDesc(
                    eq(USER_ID), eq(LocalDate.of(2025, 12, 31))))
                    .thenReturn(Optional.of(decSnapshot));
            
            BalanceSheetService.YearlyBalanceSheet sheet = balanceSheetService.getYearly(USER_ID, 2025);

            assertThat(sheet.getClosingNetWorth()).isEqualByComparingTo("150000");
        }

        @Test
        @DisplayName("Calculates net worth change as closing minus opening")
        void yearly_calculates_net_worth_change() {
            Snapshot janSnapshot = new Snapshot();
            janSnapshot.setNetWorth(new BigDecimal("100000"));

            Snapshot decSnapshot = new Snapshot();
            decSnapshot.setNetWorth(new BigDecimal("150000"));

            when(snapshotRepository.findTopByUserIdAndSnapshotDateLessThanEqualOrderBySnapshotDateDesc(
                    eq(USER_ID), eq(LocalDate.of(2025, 1, 1))))
                    .thenReturn(Optional.of(janSnapshot));

            when(snapshotRepository.findTopByUserIdAndSnapshotDateLessThanEqualOrderBySnapshotDateDesc(
                    eq(USER_ID), eq(LocalDate.of(2025, 12, 31))))
                    .thenReturn(Optional.of(decSnapshot));

            BalanceSheetService.YearlyBalanceSheet sheet = balanceSheetService.getYearly(USER_ID, 2025);

            // $150,000 - $100,000 = $50,000
            assertThat(sheet.getNetWorthChange()).isEqualByComparingTo("50000.00");
        }

        @Test
        @DisplayName("Net worth change is null when snapshots are missing")
        void yearly_net_worth_change_null_when_snapshots_missing() {
            when(snapshotRepository.findTopByUserIdAndSnapshotDateLessThanEqualOrderBySnapshotDateDesc(
                    any(), any()))
                    .thenReturn(Optional.empth());
            
            BalanceSheetService.YearlyBalanceSheet sheet = balanceSheetService.getYearly(USER_ID, 2025);

            assertThat(sheet.getNetWorthChange()).isNull();
        }
    }
    
}