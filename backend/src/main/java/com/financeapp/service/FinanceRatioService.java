package com.financeapp.service;

import com.financeapp.entity.User;
import com.financeapp.repository.AssetRepository;
import com.financeapp.repository.ExpenseRepository;
import com.financeapp.repository.IncomeRepository;
import com.financeapp.repository.LiabilityRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Calculated all finance ratios for a user and compares them
 * against age-adjusted benchmarks.
 * 
 * Ratio calculated:
 * 1. Savings Rate
 * 2. Debt-to-Income Ratio
 * 3. Emergency Fund Ratio
 * 4. Net Worth Ratio (Wealth Index)
 * 5. Liquidity Ratio
 * 6. Debt-to-Asset Ratio
 */

@Service
public class FinanceRatioService {
    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;
    private final AssetRepository assetRepository;
    private final LiabilityRepository liabilityRepository;

    public FinanceRatioService(IncomeRepository incomeRepository, ExpenseRepository expenseRepository,
            AssetRepository assetRepository, LiabilityRepository liabilityRepository) {
        this.incomeRepository = incomeRepository;
        this.expenseRepository = expenseRepository;
        this.assetRepository = assetRepository;
        this.liabilityRepository = liabilityRepository;
    }

    // Public API

    /**
     * Calculates all ratios for a user and returns them as a map of
     * ratio name -> RatioResult (value, benchmark, status, recommendation).
     */
    public Map<String, RatioResult> calculateAll(User user) {
        Map<String, RatioResult> results = new LinkedHashMap<>();

        // Date range for current month income/expense calculations
        YearMonth currentMonth = YearMonth.now();
        LocalDate from = currentMonth.atDay(1);
        LocalDate to = currentMonth.atEndOfMonth();

        BigDecimal monthlyIncome = incomeRepository.sumByUserIdAndDateRange(user.getId(), from, to);
        BigDecimal monthlyExpenses = expenseRepository.sumByUserIdAndDateRange(user.getId(), from, to);
        BigDecimal totalAssets = assetRepository.sumBalanceByUserId(user.getId());
        BigDecimal liquidAssets = assetRepository.sumLiquidAssetsByUserId(user.getId());
        BigDecimal totalLiabilities = liabilityRepository.sumBalanceByUserId(user.getId());
        BigDecimal monthlyDebtPayments = liabilityRepository.sumMonthlyPaymentsByUserId(user.getId());

        String bracket = user.getAgeBracket();
        int age = user.getAge();

        results.put("savingsRate", calculateSavingsRate(monthlyIncome, monthlyExpenses, bracket));
        results.put("debtToIncome", calculateDebtToIncome(monthlyDebtPayments, monthlyIncome, bracket));
        results.put("emergencyFund", calculateEmergencyFund(liquidAssets, monthlyExpenses, bracket));
        results.put("netWorthRatio", calculateNetWorthRatio(totalAssets, totalLiabilities, monthlyIncome, age));
        results.put("liquidityRatio", calculateLiquidityRatio(liquidAssets, monthlyExpenses, bracket));
        results.put("debtToAsset", calculateDebtToAsset(totalLiabilities, totalAssets, bracket));

        return results;
    }

    // Ratio calculations

    /**
     * Savings Rate = (Income - Expenses) / Income * 100
     * 
     * Benchmarks by age:
     * 20s -> aim for 10 - 15%
     * 30s -> aim for 15 - 20%
     * 40s -> aim for 20 - 25%
     * 50s+ -> aim for 20 - 30%
     * 60+ -> aim for 30%+
     */
    private RatioResult calculateSavingsRate(BigDecimal income, BigDecimal expenses, String bracket) {
        if (income.compareTo(BigDecimal.ZERO) == 0) {
            return RatioResult.noData("savingsRate", "No income recorded for this month");
        }

        BigDecimal savings = income.subtract(expenses);
        BigDecimal rate = savings.divide(income, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        double[] benchmarks = savingsRateBenchmark(bracket);
        String status = rate.doubleValue() >= benchmarks[1] ? "GOOD"
                : rate.doubleValue() >= benchmarks[0] ? "WARNING" : "CRITICAL";

        String recommendation = switch (bracket) {
            case "20s" ->
                "In your 20s, aim to save at least 10-15% of income. Even small amounts compound significantly over tiem.";
            case "30s" ->
                "In your 30s, target 15-20%. This is a key decade for wealth building - maximize any employer 401k match.";
            case "40s" ->
                "In your 40s, push for 20-25%. If you're behind on savings, now is the time to accelerate contributions.";
            case "50s" -> "In your 50s, sim for 25-30% and take advantage of catch-up contributions.";
            default ->
                "At 60+, save as aggressively as possible and review your retirement income plan with an advisor.";
        };

        return new RatioResult("Savings Rate", rate, benchmarks[0], benchmarks[1], "%", status, bracket,
                recommendation);
    }

    /**
     * Debt-to-Income Ratio = Monthly Debt Payments / Monthly Income * 100
     * 
     * Lower is better. Above 43% make it difficult to qualify for loans.
     * 
     * Benchmarks:
     * 20s -> 36%
     * 30s -> 36%
     * 40s -> 30%
     * 50s -> 20%
     * 60+ 10%
     * 
     * Industry standard threshold: 36% max
     */
    private RatioResult calculateDebtToIncome(BigDecimal monthlyDebt, BigDecimal income, String bracket) {
        if (income.compareTo(BigDecimal.ZERO) == 0) {
            return RatioResult.noData("debtToIncome", "No income recorded for this month");
        }

        BigDecimal ratio = monthlyDebt.divide(income, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        double benchmark = 36.0;
        String status = ratio.doubleValue() <= benchmark * 0.75 ? "GOOD"
                : ratio.doubleValue() <= benchmark ? "WARNING" : "CRITICAL";

        String recommendation = "Keep total monthly debt payments at or below 36% of gross income. This is the standard threshold used by lenders. Above 43% will make qualifying for new credit difficult.";

        return new RatioResult("Debt-to-Income", ratio, 0, benchmark, "%", status, bracket, recommendation);
    }

    /**
     * Emergency Fund = Liquid Assets / Monthly Expenses
     * Measures of how many months of expenses are covered by liquid savings.
     * 
     * Benchmarks:
     * 20s -> 3 months
     * 30s -> 3-6 months
     * 40s -> 6 months
     * 50s -> 6-9 months
     * 60+ -> 12 months
     */
    private RatioResult calculateEmergencyFund(BigDecimal liquidAssets, BigDecimal monthlyExpenses, String bracket) {
        if (monthlyExpenses.compareTo(BigDecimal.ZERO) == 0) {
            return RatioResult.noData("emergencyFund", "No expenses recorded for this month");
        }

        BigDecimal ratio = liquidAssets.divide(monthlyExpenses, 2, RoundingMode.HALF_UP);
        double[] benchmarks = emergencyFundBenchmark(bracket);
        String status = ratio.doubleValue() >= benchmarks[1] ? "GOOD"
                : ratio.doubleValue() >= benchmarks[0] ? "WARNING" : "CRITICAL";

        String recommendation = switch (bracket) {
            case "20s" -> "Build up to 3 months of expenses.";
            case "30s" -> "Target 3-6 months.";
            case "40s" -> "Aim for 6 months.";
            case "50s" -> "Target 6-9 months of expenses.";
            default -> "Keep 12 months of expenses liquid.";
        };

        return new RatioResult("Emergency Fund", ratio, benchmarks[0], benchmarks[1], " months", status, bracket,
                recommendation);
    }

    /**
     * Net Worth Ratio (Wealth Index) = Net Worth / Annual Income
     * 
     * Expected net worth = age * annual income / 10
     * target ratio: age / 10
     */
    private RatioResult calculateNetWorthRatio(BigDecimal totalAssets, BigDecimal totalLiabilities,
            BigDecimal monthlyIncome, int age) {
        BigDecimal annualIncome = monthlyIncome.multiply(BigDecimal.valueOf(12));
        if (annualIncome.compareTo(BigDecimal.ZERO) == 0) {
            return RatioResult.noData("netWorthRatio", "No income recorded for this month");
        }

        BigDecimal netWorth = totalAssets.subtract(totalLiabilities);
        BigDecimal ratio = netWorth.divide(annualIncome, 2, RoundingMode.HALF_UP);

        double target = age / 10.0;
        double good = target;
        double warning = target * 0.5;

        String status = ratio.doubleValue() >= good ? "GOOD" : ratio.doubleValue() >= warning ? "WARNING" : "CRITICAL";

        String recommendation = String.format(
                "At age %d, your target net worth is %,1fx your annual income ($%.0f). ", age, target,
                annualIncome.multiply(BigDecimal.valueOf(target)).doubleValue());

        return new RatioResult("Net Worth Ratio", ratio, warning, good, "x income", status, user(age), recommendation);
    }

    /**
     * Liquidty Ratio = Liquid Assets / Monthly Expenses
     * Similar to emergency fund ratio but focused on immediate cash availability.
     * A ratio of 1.0 means you have exactly one month of expenses in cash.
     */
    private RatioResult calculateLiquidityRatio(BigDecimal liquidAssets, BigDecimal monthlyExpenses, String bracket) {
        if (monthlyExpenses.compareTo(BigDecimal.ZERO) == 0) {
            return RatioResult.noData("liquidityRatio", "No expenses recorded for this month");
        }

        BigDecimal ratio = liquidAssets.divide(monthlyExpenses, 2, RoundingMode.HALF_UP);

        double min = 1.0;
        double good = 3.0;
        String status = ratio.doubleValue() >= good ? "GOOD" : ratio.doubleValue() >= min ? "WARNING" : "CRITICAL";

        String recommendation = "Maintain at least 1 month of expenses in liquid cash at all times. 3+ months provide a comfortable buffer for unexpected expenses without disrupting investments.";

        return new RatioResult("Liquidity Ratio", ratio, min, good, " months", status, bracket, recommendation);
    }

    /**
     * Debt-to-Asset Ratio = Total Liabilities / Total Assets * 100
     * Measures the percentage of assets that are financed by debt.
     * Lower is better. 0% means debt-free.
     * 
     * Benchmarks:
     * 20s -> 75% or lower (mortgage + student loan are common)
     * 30s -> 60%
     * 40s -> 40%
     * 50s -> 25%
     * 60+ -> 10%
     */
    private RatioResult calculateDebtToAsset(BigDecimal liabilities, BigDecimal assets, String bracket) {
        if (assets.compareTo(BigDecimal.ZERO) == 0) {
            return RatioResult.noData("debtToAsset", "No assets recorded for this month");
        }

        BigDecimal ratio = liabilities.divide(assets, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        double maxGood = debtToAssetBenchmark(bracket);
        String status = ratio.doubleValue() <= maxGood * 0.6 ? "GOOD"
                : ratio.doubleValue() <= maxGood ? "WARNING" : "CRITICAL";

        String recommendation = switch (bracket) {
            case "20s" -> "A high debt-to-asset ratio is common in your 20s due to student loans and mortagegs.";
            case "30s" -> "Aim to reduce below 60% by consistently paying down the debt.";
            case "40s" -> "Target below 40%.";
            case "50s" -> "Get below 25%.";
            default -> "Keep below 10% in retirement.";
        };

        return new RatioResult("Debt-to-Asset Ratio", ratio, 0, maxGood, "%", status, bracket, recommendation);
    }

    // Benchmarks

    private double[] savingsRateBenchmark(String bracket) {
        return switch (bracket) {
            case "20s" -> new double[] { 10, 15 };
            case "30s" -> new double[] { 15, 20 };
            case "40s" -> new double[] { 20, 25 };
            case "50s" -> new double[] { 25, 30 };
            default -> new double[] { 30, 35 };
        };
    }

    private double[] emergencyFundBenchmark(String bracket) {
        return switch (bracket) {
            case "20s" -> new double[] { 3, 3 };
            case "30s" -> new double[] { 3, 6 };
            case "40s" -> new double[] { 6, 6 };
            case "50s" -> new double[] { 6, 9 };
            default -> new double[] { 12, 12 };
        };
    }

    private double debtToAssetBenchmark(String bracket) {
        return switch (bracket) {
            case "20s" -> 75.0;
            case "30s" -> 60.0;
            case "40s" -> 40.0;
            case "50s" -> 25.0;
            default -> 10.0;
        };
    }

    // Helper to convert age to bracket label for net worth ratio
    private String user(int age) {
        if (age < 30)
            return "20s";
        if (age < 40)
            return "30s";
        if (age < 50)
            return "40s";
        if (age < 60)
            return "50s";
        return "60+";
    }

    // RatioResult inner class

    /**
     * Holda the result of a single ratio calculation.
     * Returned to the controller and serialized to JSON.
     */
    public static class RatioResult {
        private final String name;
        private final BigDecimal value;
        private final double benchmarkMin;
        private final double benchmarkMax;
        private final String unit;
        private final String status; // GOOD, WARNING, CRITICAL
        private final String ageBracket;
        private final String recommendation;

        public RatioResult(String name, BigDecimal value, double benchmarkMin, double benchmarkMax, String unit,
                String status, String ageBracket, String recommendation) {
            this.name = name;
            this.value = value;
            this.benchmarkMin = benchmarkMin;
            this.benchmarkMax = benchmarkMax;
            this.unit = unit;
            this.status = status;
            this.ageBracket = ageBracket;
            this.recommendation = recommendation;
        }

        public static RatioResult noData(String name, String reason) {
            return new RatioResult(name, BigDecimal.ZERO, 0, 0, "", "NO_DATA", "N/A", reason);
        }

        public String getName() {
            return name;
        }

        public BigDecimal getValue() {
            return value;
        }

        public double getBenchmarkMin() {
            return benchmarkMin;
        }

        public double getBenchmarkMax() {
            return benchmarkMax;
        }

        public String getUnit() {
            return unit;
        }

        public String getStatus() {
            return status;
        }

        public String getAgeBracket() {
            return ageBracket;
        }

        public String getRecommendation() {
            return recommendation;
        }
    }
}