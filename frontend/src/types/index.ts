// Users
export interface UserResponse {
    id: number;
    email: string;
    firstName: string;
    lastName: string;
    fullName: string;
    dateOfBirth: string;
    age: number;
    ageBracket: string;
    createdAt: string;
}

export interface UserRequest {
    email: string;
    firstName: string;
    lastName: string;
    dateOfBirth: string;
}

// Income
export interface IncomeResponse {
    id: number;
    userId: number;
    amount: number;
    source: string;
    receivedAt: string;
    createdAt: string;
}

export interface IncomeRequest {
    amount: number;
    source: string;
    receivedAt: string;
}

// Expenses
export interface ExpenseResponse {
    id: number;
    userId: number;
    amount: number;
    category: string;
    description: string | null;
    spentAt: string;
    createdAt: string;
}

export interface ExpenseRequest {
    amount: number;
    category: string;
    description?: string | null;
    spentAt: string;
}

// Assets
export type AssetType = 'Savings' | 'Investment' | 'Property' | 'Retirement' | 'Other';

export interface AssetResponse {
    id: number;
    userId: number;
    name: string;
    type: AssetType;
    balance: number;
    updatedAt: string;
}

export interface AssetRequest {
    name: string;
    type: AssetType;
    balance: number;
}

// Liabilities
export type LiabilityType = 'Mortgage' | 'Auto' | 'Student' | 'Credit Card' | 'Personal' | 'Other';

export interface LiabilityResponse {
    id: number;
    userId: number;
    name: string;
    type: LiabilityType;
    balance: number;
    interestRate: number | null;
    monthlyPayment: number | null;
    updatedAt: string;
}

export interface LiabilityRequest {
    name: string;
    type: LiabilityType;
    balance: number;
    interestRate?: number | null;
    monthlyPayment?: number | null;
}

// Retirement Accounts
export interface RetirementAccountResponse {
    id: number;
    userId: number;
    name: string;
    provider: string | null;
    balance: number;
    monthlyContribution: number;
    employerMatch: number | null;
    expectedReturnRate: number | null;
    totalMonthlyContribution: number;
    updatedAt: string;
}

export interface RetirementAccountRequest {
    name: string;
    provider?: string | null;
    balance: number;
    monthlyContribution: number;
    employerMatch?: number | null;
    expectedReturnRate?: number | null;
}

// Income Allocations
export type AllocationType = 'PERCENT' | 'FIXED';

export interface IncomeAllocationResponse {
    id: number;
    userId: number;
    category: string;
    allocationType: AllocationType;
    allocationValue: number;
    priority: number;
    createdAt: string;
}

export interface IncomeAllocationRequest {
    category: string;
    allocationType: AllocationType;
    allocationValue: number;
    priority: number;
}

// Snapshots
export interface SnapshotResponse {
    id: number;
    userId: number;
    snapshotDate: string;
    totalAssets: number;
    totalLiabilities: number;
    netWorth: number;
    createdAt: string;
}

// Ratios
export type RatioStatus = 'GOOD' | 'WARNING' | 'CRITICAL' | 'NO_DATA';

export interface RatioResult {
    name: string;
    value: number;
    benchmarkMin: number;
    benchmarkMax: number;
    unit: string;
    status: RatioStatus;
    ageBracket: string;
    recommendation: string;
}

export interface RatioResponse {
    savingsRate: RatioResult;
    debtToIncome: RatioResult;
    emergencyFund: RatioResult;
    netWorthRatio: RatioResult;
    liquidityRatio: RatioResult;
    debtToAsset: RatioResult;
}

// Retirement Projection
export interface YearlyValue {
    year: number;
    balance: number;
}

export interface AccountProjection {
    accountId: number;
    accountName: string;
    provider: string | null;
    currentBalance: number;
    totalMonthlyContribution: number;
    annualReturnRate: number;
    projectedBalance: number;
    totalContributed: number;
    totalGrowth: number;
    yearlyBreakdown: YearlyValue[];
}

export interface ProjectionSummary {
    currentAge: number;
    retirementAge: number;
    yearsToRetirement: number;
    totalProjectedBalance: number;
    estimatedAnnualIncome: number;
    estimatedMonthlyIncome: number;
    accounts: AccountProjection[];
}

// Balance Sheet
export interface CategoryTotal {
    category: string;
    amount: number;
}

export interface MonthlyBalanceSheet {
    period: string;
    periodLabel: string;
    totalIncome: number;
    totalExpenses: number;
    netCashFlow: number;
    savingsRate: number;
    expensesByCategory: CategoryTotal[];
    openingNetWorth: number | null;
    closingNetWorth: number | null;
}

export interface YearlyBalanceSheet {
    year: number;
    months: MonthlyBalanceSheet[];
    annualIncome: number;
    annualExpenses: number;
    annualNetCashFlow: number;
    annualSavingsRate: number;
    openingNetWorth: number | null;
    closingNetWorth: number | null;
    netWorthChange: number | null;
}

// Toast
export type ToastType = 'success' | 'error' | 'info';

export interface Toast {
    id: number;
    message: string;
    type: ToastType;
}