package com.financeapp.service;

import com.financeapp.entity.Snapshot;
import com.financeapp.repository.ExpenseRepository;
import com.financeapp.repository.IncomeRepository;
import com.financeapp.repository.SnapshotRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Generates monthly and yearly balance sheets by aggregating
 * income, expenses, and snapshots for a given user and period.
 *
 * Monthly balance sheet:
 * - Total income for the month
 * - Total expenses broken down by category
 * - Net cash flow (income - expenses)
 * - Savings rate for the month
 * - Opening and closing net worth from snapshots
 *
 * Yearly balance sheet:
 * - Month-by-month summary rows for the full year
 * - Annual totals for income, expenses, and net cash flow
 * - Year-over-year net worth change
 */
@Service
public class BalanceSheetService {
    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;
    private final SnapshotRepository snapshotRepository;

    public BalanceSheetService(IncomeRepository incomeRepository, ExpenseRepository expenseRepository,
            SnapshotRepository snapshotRepository) {
        this.incomeRepository = incomeRepository;
        this.expenseRepository = expenseRepository;
        this.snapshotRepository = snapshotRepository;
    }

    // Monthly balance sheet

    /**
     * Generates a balance sheet for a single calendar month.
     *
     * @param userId the user
     * @param year   e.g. 2025
     * @param month  e.g. 3 (March)
     */
    public MonthlyBalanceSheet getMonthly(Long userId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate from = yearMonth.atDay(1);
        LocalDate to = yearMonth.atEndOfMonth();

        BigDecimal totalIncome = incomeRepository.sumByUserIdAndDateRange(userId, from, to);
        BigDecimal totalExpenses = expenseRepository.sumByUserIdAndDateRange(userId, from, to);
        BigDecimal netCashFlow = totalIncome.subtract(totalExpenses);

        // Savings rate = net cash flow / income * 100
        BigDecimal savingsRate = totalIncome.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO
                : netCashFlow.divide(totalIncome, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2,
                        RoundingMode.HALF_UP);

        // Expense breakdown by category
        List<CategoryTotal> expenseByCategory = getCategoryTotals(userId, from, to);

        // Opening net worth = nearest snapshot on or before first day of the month
        BigDecimal openingNetWorth = snapshotRepository
                .findTopByUserIdAndSnapshotDateLessThanEqualOrderBySnapshotDateDesc(userId, from)
                .map(Snapshot::getNetWorth)
                .orElse(null);

        // Closing net worth - nearest snapshot on or before last day of the month
        BigDecimal closingNetWorth = snapshotRepository
                .findTopByUserIdAndSnapshotDateLessThanEqualOrderBySnapshotDateDesc(userId, to)
                .map(Snapshot::getNetWorth)
                .orElse(null);

        String monthLabel = Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + year;

        return new MonthlyBalanceSheet(
                yearMonth.toString(),
                monthLabel,
                totalIncome.setScale(2, RoundingMode.HALF_UP),
                totalExpenses.setScale(2, RoundingMode.HALF_UP),
                netCashFlow.setScale(2, RoundingMode.HALF_UP),
                savingsRate,
                expenseByCategory,
                openingNetWorth,
                closingNetWorth);
    }

    // Yearly balance sheet

    /**
     * Generates a balance sheet for a full calendar year.
     * Returns a row for each month plus annual totals.
     *
     * @param userId the user
     * @param year   e.g. 2025
     */
    public YearlyBalanceSheet getYearly(Long userId, int year) {
        List<MonthlyBalanceSheet> months = new ArrayList<>();

        BigDecimal annualIncome = BigDecimal.ZERO;
        BigDecimal annualExpenses = BigDecimal.ZERO;

        for (int m = 1; m <= 12; m++) {
            MonthlyBalanceSheet monthly = getMonthly(userId, year, m);
            months.add(monthly);
            annualIncome = annualIncome.add(monthly.getTotalIncome());
            annualExpenses = annualExpenses.add(monthly.getTotalExpenses());
        }

        BigDecimal annualNetCashFlow = annualIncome.subtract(annualExpenses);
        BigDecimal annualSavingsRate = annualIncome.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO
                : annualNetCashFlow.divide(annualIncome, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);

        // Yearly opening net worth - nearest snapshot on or before Jan 1
        BigDecimal openingNetWorth = snapshotRepository
                .findTopByUserIdAndSnapshotDateLessThanEqualOrderBySnapshotDateDesc(userId, LocalDate.of(year, 1, 1))
                .map(Snapshot::getNetWorth)
                .orElse(null);

        // Yearly closing net worth - nearest snapshot on or before Dec 31
        BigDecimal closingNetWorth = snapshotRepository
                .findTopByUserIdAndSnapshotDateLessThanEqualOrderBySnapshotDateDesc(userId, LocalDate.of(year, 12, 31))
                .map(Snapshot::getNetWorth)
                .orElse(null);

        // Net worth change over the year
        BigDecimal netWorthChange = null;
        if (openingNetWorth != null && closingNetWorth != null) {
            netWorthChange = closingNetWorth.subtract(openingNetWorth).setScale(2, RoundingMode.HALF_UP);
        }

        return new YearlyBalanceSheet(
                year,
                months,
                annualIncome.setScale(2, RoundingMode.HALF_UP),
                annualExpenses.setScale(2, RoundingMode.HALF_UP),
                annualNetCashFlow.setScale(2, RoundingMode.HALF_UP),
                annualSavingsRate,
                openingNetWorth,
                closingNetWorth,
                netWorthChange);
    }

    // Helpers
    /**
     * Returns total spending per category for the given date range.
     * Used for the epense breakdown section of the monthly balance sheet.
     */
    private List<CategoryTotal> getCategoryTotals(Long userId, LocalDate from, LocalDate to) {
        List<Object[]> rows = expenseRepository.sumByCategoryAndDateRange(userId, from, to);
        List<CategoryTotal> totals = new ArrayList<>();
        for (Object[] row : rows) {
            String category = (String) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            totals.add(new CategoryTotal(category, amount.setScale(2, RoundingMode.HALF_UP)));
        }
        return totals;
    }

    // Result Classes

    public static class MonthlyBalanceSheet {
        private final String period;
        private final String periodLabel;
        private final BigDecimal totalIncome;
        private final BigDecimal totalExpenses;
        private final BigDecimal netCashFlow;
        private final BigDecimal savingsRate;
        private final List<CategoryTotal> expensesByCategory;
        private final BigDecimal openingNetWorth;
        private final BigDecimal closingNetWorth;

        public MonthlyBalanceSheet(String period, String periodLabel, BigDecimal totalIncome, BigDecimal totalExpenses,
                BigDecimal netCashFlow, BigDecimal savingsRate, List<CategoryTotal> expensesByCategory,
                BigDecimal openingNetWorth, BigDecimal closingNetWorth) {
            this.period = period;
            this.periodLabel = periodLabel;
            this.totalIncome = totalIncome;
            this.totalExpenses = totalExpenses;
            this.netCashFlow = netCashFlow;
            this.savingsRate = savingsRate;
            this.expensesByCategory = expensesByCategory;
            this.openingNetWorth = openingNetWorth;
            this.closingNetWorth = closingNetWorth;
        }

        public String getPeriod() {
            return period;
        }

        public String getPeriodLabel() {
            return periodLabel;
        }

        public BigDecimal getTotalIncome() {
            return totalIncome;
        }

        public BigDecimal getTotalExpenses() {
            return totalExpenses;
        }

        public BigDecimal getNetCashFlow() {
            return netCashFlow;
        }

        public BigDecimal getSavingsRate() {
            return savingsRate;
        }

        public List<CategoryTotal> getExpensesByCategory() {
            return expensesByCategory;
        }

        public BigDecimal getOpeningNetWorth() {
            return openingNetWorth;
        }

        public BigDecimal getClosingNetWorth() {
            return closingNetWorth;
        }
    }

    public static class YearlyBalanceSheet {
        private final int year;
        private final List<MonthlyBalanceSheet> months;
        private final BigDecimal annualIncome;
        private final BigDecimal annualExpenses;
        private final BigDecimal annualNetCashFlow;
        private final BigDecimal annualSavingsRate;
        private final BigDecimal openingNetWorth;
        private final BigDecimal closingNetWorth;
        private final BigDecimal netWorthChange;

        public YearlyBalanceSheet(int year, List<MonthlyBalanceSheet> months, BigDecimal annualIncome,
                BigDecimal annualExpenses, BigDecimal annualNetCashFlow, BigDecimal annualSavingsRate,
                BigDecimal openingNetWorth, BigDecimal closingNetWorth, BigDecimal netWorthChange) {
            this.year = year;
            this.months = months;
            this.annualIncome = annualIncome;
            this.annualExpenses = annualExpenses;
            this.annualNetCashFlow = annualNetCashFlow;
            this.annualSavingsRate = annualSavingsRate;
            this.openingNetWorth = openingNetWorth;
            this.closingNetWorth = closingNetWorth;
            this.netWorthChange = netWorthChange;
        }

        public int getYear() {
            return year;
        }

        public List<MonthlyBalanceSheet> getMonths() {
            return months;
        }

        public BigDecimal getAnnualIncome() {
            return annualIncome;
        }

        public BigDecimal getAnnualExpenses() {
            return annualExpenses;
        }

        public BigDecimal getAnnualNetCashFlow() {
            return annualNetCashFlow;
        }

        public BigDecimal getAnnualSavingsRate() {
            return annualSavingsRate;
        }

        public BigDecimal getOpeningNetWorth() {
            return openingNetWorth;
        }

        public BigDecimal getClosingNetWorth() {
            return closingNetWorth;
        }

        public BigDecimal getNetWorthChange() {
            return netWorthChange;
        }
    }

    public static class CategoryTotal {
        private final String category;
        private final BigDecimal amount;

        public CategoryTotal(String category, BigDecimal amount) {
            this.category = category;
            this.amount = amount;
        }

        public String getCategory() {
            return category;
        }

        public BigDecimal getAmount() {
            return amount;
        }
    }
}