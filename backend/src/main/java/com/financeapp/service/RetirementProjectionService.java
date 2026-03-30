package com.financeapp.service;

import com.financeapp.entity.RetirementAccount;
import com.financeapp.entity.User;
import com.financeapp.repository.RetirementAccountRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

/**
 * Projects retirement savings growth using compound interest formula.
 * 
 * Formula (Future Value of annuity with existing balance):
 * FV = P * (1 + r)^n + PMT * [((1 + r)^(n - 1) / r]
 * 
 * Where:
 * P = current balance
 * r = monthly interest rate
 * n = total months until retirement
 * PMT = total monthly contribution
 * 
 * Retirement income is estimated using the 4% rule:
 * Annual Income = Total Projected Balance * 0.04
 * Monthly Income = Annual Income / 12
 */
@Service
public class RetirementProjectionService {

    // Default retirment age unless overridden
    private static final int DEFAULT_RETIREMENT_AGE = 65;

    // 4% rule for sustainable annual withdrawal
    private static final double WITHDRAWAL_RATE = 0.04;

    private final RetirementAccountRepository retirementAccountRepository;

    public RetirementProjectionService(RetirementAccountRepository retirementAccountRepository) {
        this.retirementAccountRepository = retirementAccountRepository;
    }

    // Public API

    /**
     * Projects all retirement accounts for a user and returns a full
     * ProjectionSummary containing per-account projections, totals,
     * and estimated monthly retirement income.
     * 
     * @param user          The user to project for
     * @param retirementAge target retirement age (defaults to 65 if null)
     */
    public ProjectionSummary projectAll(User user, Integer retirementAge) {
        int targetAge = (retirementAge != null) ? retirementAge : DEFAULT_RETIREMENT_AGE;
        int monthsToRetirement = calculateMonthsToRetirement(user.getDateOfBirth(), targetAge);

        if (monthsToRetirement <= 0) {
            return ProjectionSummary.alreadyRetired(user.getAge(), targetAge);
        }

        List<RetirementAccount> accounts = retirementAccountRepository.findByUserId(user.getId());

        List<AccountProjection> accountProjections = new ArrayList<>();
        BigDecimal totalProjectedBalance = BigDecimal.ZERO;

        for (RetirementAccount account : accounts) {
            AccountProjection projection = projectAccount(account, monthsToRetirement);
            accountProjections.add(projection);
            totalProjectedBalance = totalProjectedBalance.add(projection.getProjectedBalance());
        }

        // Estimated monthly retirement income using 4% rule
        BigDecimal annualRetirementIncome = totalProjectedBalance
                .multiply(BigDecimal.valueOf(WITHDRAWAL_RATE))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal monthlyRetirementIncome = annualRetirementIncome.divide(BigDecimal.valueOf(12), 2,
                RoundingMode.HALF_UP);

        int yearsToRetirement = monthsToRetirement / 12;

        return new ProjectionSummary(
                user.getAge(),
                targetAge,
                yearsToRetirement,
                accountProjections,
                totalProjectedBalance.setScale(2, RoundingMode.HALF_UP),
                annualRetirementIncome,
                monthlyRetirementIncome);
    }

    // Projection logic

    /**
     * Projects a single retirement acocunt over the f=given number of months.
     * Generates a year-by-year breakdown.
     */
    private AccountProjection projectAccount(RetirementAccount account, int totalMonths) {
        BigDecimal P = account.getBalance();
        BigDecimal PMT = account.getTotalMonthlyContribution();

        // Use default 7% return is not set
        double annualRate = account.getExpectedReturnRate() != null ? account.getExpectedReturnRate().doubleValue()
                : 7.0;
        double monthlyRate = annualRate / 12.0 / 100.0;

        List<YearlyValue> yearlyBreakdown = new ArrayList<>();
        BigDecimal runningBalance = P;
        int totalYears = totalMonths / 12;

        for (int year = 1; year <= totalYears; year++) {
            // Apply 12 months of compound growth + contributions
            runningBalance = calculateFutureValue(runningBalance, PMT, monthlyRate, 12);

            yearlyBreakdown.add(new YearlyValue(year, runningBalance.setScale(2, RoundingMode.HALF_UP)));
        }

        // Handle remaining months after the last full year
        int remainingMonths = totalMonths % 12;
        if (remainingMonths > 0) {
            runningBalance = calculateFutureValue(runningBalance, PMT, monthlyRate, remainingMonths);
        }

        BigDecimal projectedBalance = runningBalance.setScale(2, RoundingMode.HALF_UP);

        // Total contributed over the projection period (excluding growth)
        BigDecimal totalContributed = PMT
                .multiply(BigDecimal.valueOf(totalMonths))
                .add(P)
                .setScale(2, RoundingMode.HALF_UP);

        // Growth = projected balance - total contributed
        BigDecimal totalGrowth = projectedBalance.subtract(totalContributed)
                .setScale(2, RoundingMode.HALF_UP);

        return new AccountProjection(
                account.getId(),
                account.getName(),
                account.getProvider(),
                account.getBalance(),
                PMT,
                annualRate,
                projectedBalance,
                totalContributed,
                totalGrowth,
                yearlyBreakdown);
    }

    /**
     * Future Value formula:
     * FV = P * (1 + r)^n + PMT * [((1 + r)^n - 1) / r]
     * 
     * If r == 0, simplifies to FV = P + PMT * n
     */
    private BigDecimal calculateFutureValue(BigDecimal P, BigDecimal PMT, double monthlyRate, int months) {
        if (monthlyRate == 0) {
            return P.add(PMT.multiply(BigDecimal.valueOf(months)));
        }

        MathContext mc = new MathContext(10, RoundingMode.HALF_UP);
        double growthFactor = Math.pow(1 + monthlyRate, months);
        BigDecimal principalGrowth = P.multiply(BigDecimal.valueOf(growthFactor), mc);
        BigDecimal contributionGrowth = PMT.multiply(BigDecimal.valueOf((growthFactor - 1) / monthlyRate), mc);

        return principalGrowth.add(contributionGrowth, mc);
    }

    /**
     * Calculate months from today until the user reaches the target retirement age.
     */
    private int calculateMonthsToRetirement(LocalDate dateOfBirth, int retirementAge) {
        LocalDate retirementDate = dateOfBirth.plusYears(retirementAge);
        Period period = Period.between(LocalDate.now(), retirementDate);
        return Math.max(0, period.getYears() * 12 + period.getMonths());
    }

    // Result Classes

    /**
     * Full projection summary returned by the API
     */
    public static class ProjectionSummary {
        private final int currentAge;
        private final int retirementAge;
        private final int yearsToRetirement;
        private final List<AccountProjection> accounts;
        private final BigDecimal totalProjectedBalance;
        private final BigDecimal estimatedAnnualIncome;
        private final BigDecimal estimatedMonthlyIncome;

        public ProjectionSummary(int currentAge, int retirementAge, int yearsToRetirement,
                List<AccountProjection> accounts, BigDecimal totalProjectedBalance, BigDecimal estimatedAnnualIncome,
                BigDecimal estimatedMonthlyIncome) {
            this.currentAge = currentAge;
            this.retirementAge = retirementAge;
            this.yearsToRetirement = yearsToRetirement;
            this.accounts = accounts;
            this.totalProjectedBalance = totalProjectedBalance;
            this.estimatedAnnualIncome = estimatedAnnualIncome;
            this.estimatedMonthlyIncome = estimatedMonthlyIncome;
        }

        public static ProjectionSummary alreadyRetired(int currentAge, int retirementAge) {
            return new ProjectionSummary(
                    currentAge,
                    retirementAge,
                    0,
                    new ArrayList<>(),
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO);
        }

        public int getCurrentAge() {
            return currentAge;
        }

        public int getRetirementAge() {
            return retirementAge;
        }

        public int getYearsToRetirement() {
            return yearsToRetirement;
        }

        public List<AccountProjection> getAccounts() {
            return accounts;
        }

        public BigDecimal getTotalProjectedBalance() {
            return totalProjectedBalance;
        }

        public BigDecimal getEstimatedAnnualIncome() {
            return estimatedAnnualIncome;
        }

        public BigDecimal getEstimatedMonthlyIncome() {
            return estimatedMonthlyIncome;
        }
    }

    /**
     * Projection result for a single retirement account.
     */
    private static class AccountProjection {
        private final Long accountId;
        private final String accountName;
        private final String provider;
        private final BigDecimal currentBalance;
        private final BigDecimal totalMonthlyContribution;
        private final double annualReturnRate;
        private final BigDecimal projectedBalance;
        private final BigDecimal totalContributed;
        private final BigDecimal totalGrowth;
        private final List<YearlyValue> yearlyBreakdown;

        public AccountProjection(Long accountId, String accountName, String provider, BigDecimal currentBalance,
                BigDecimal totalMonthlyContribution, double annualReturnRate, BigDecimal projectedBalance,
                BigDecimal totalContributed, BigDecimal totalGrowth, List<YearlyValue> yearlyBreakdown) {
            this.accountId = accountId;
            this.accountName = accountName;
            this.provider = provider;
            this.currentBalance = currentBalance;
            this.totalMonthlyContribution = totalMonthlyContribution;
            this.annualReturnRate = annualReturnRate;
            this.projectedBalance = projectedBalance;
            this.totalContributed = totalContributed;
            this.totalGrowth = totalGrowth;
            this.yearlyBreakdown = yearlyBreakdown;
        }

        public Long getAccountId() {
            return accountId;
        }

        public String getAccountName() {
            return accountName;
        }

        public String getProvider() {
            return provider;
        }

        public BigDecimal getCurrentBalance() {
            return currentBalance;
        }

        public BigDecimal getTotalMonthlyContribution() {
            return totalMonthlyContribution;
        }

        public double getAnnualReturnRate() {
            return annualReturnRate;
        }

        public BigDecimal getProjectedBalance() {
            return projectedBalance;
        }

        public BigDecimal getTotalContributed() {
            return totalContributed;
        }

        public BigDecimal getTotalGrowth() {
            return totalGrowth;
        }

        public List<YearlyValue> getYearlyBreakdown() {
            return yearlyBreakdown;
        }
    }

    public static class YearlyValue {
        private final int year;
        private final BigDecimal balance;

        public YearlyValue(int year, BigDecimal balance) {
            this.year = year;
            this.balance = balance;
        }

        public int getYear() {
            return year;
        }

        public BigDecimal getBalance() {
            return balance;
        }
    }
}