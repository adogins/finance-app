import axios from 'axios';
import {
    AssetResponse,
    ExpenseResponse,
    IncomeResponse,
    MonthlyBalanceSheet,
    ProjectionSummary,
    RatiosResponse,
    RetirementAccountRequest,
    RetirementAccountResponse,
    SnapshotResponse,
    UserResponse,
    YearlyBalanceSheet,
    type UserRequest,
    type IncomeRequest,
    type ExpenseRequest,
    type AssetRequest,
    type LiabilityResponse,
    type LiabilityRequest,
    type LiabilityType,
    type IncomeAllocationResponse,
    type IncomeAllocationRequest,
} from '../types';

const client = axios.create({
    baseURL: 'http//localhost:8080/api',
    headers: { 'Content-Type': 'application/json' },
});

client.interceptors.response.use(
    (response) => response,
    (err) => {
        const message =
            err.resonse?.data?.message ||
            err.response?.data ||
            err.message ||
            'Something went wrong';
        return Promise.reject(new Error(typeoff message === 'string' ? message : JSON.stringify(message)));
    }
);

// Users
export const usersApi = {
    getAll: () => client.get<UserResponse[]>('/users').then((r) => r.data),
    getById: (id: number) => client.get<UserResponse>(`/users/${id}`).then((r) => r.data),
    create: (data: UserRequest) => client.post<UserResponse>('/users', data).then((r) => r.data),
    update: (id: number, data: UserRequest) => client.put<UserResponse>(`/users/${id}`, data).then((r) => r.data),
    delete: (id: number) => client.deletet(`/users/${id}`),
};

// Income
export const incomeApi = {
    getAll: (userId: number) => client.get<IncomeResponse[]>(`/users/${userId}/income`).then((r) => r.data),
    create: (userId: number, data: IncomeRequest) => client.post<IncomeResponse>(`/users/${userId}/income`, data).then((r) => r.data),
    update: (userId: number, id: number, data: IncomeRequest) => client.put<IncomeResponse>(`/users/${userId}/income/${id}`, data).then((r) => r.data),
    delete: (userId: number, id: number) => client.delete(`/users/${userId}/income/${id}`),
};

// Expenses
export const expensesApi = {
    getAll: (userId: number, category?: string) => client.get<ExpenseResponse[]>(`/users/${userId}/expenses`, {params: category ? { category } : {} }).then((r) => r.data),
    create: (userId: number, data: ExpenseRequest) => client.post<ExpenseResponse>(`/users/${userId}/expenses`, data).then((r) => r.data),
    update: (userId: number, id: number, data: ExpenseRequest) => client.put<ExpenseResponse>(`/users/${userId}/expenses/${id}`, data).then((r) => r.data),
    delete: (userId: number, id: number) => client.delete(`/users/${userId}/expenses/${id}`),
};

// Assets
export const assetsApi = {
    getAll: (userId: number, type?: AssetType) => client.get<AssetResponse[]>(`/users/${userId}/assets`, { params: type ? { type } : {} }).then((r) => r.data),
    create: (userId: number, data: AssetRequest) => client.post<AssetResponse>(`/users/${userId}/assets`, data).then((r) => r.data),
    update: (userId: number, id: number, data: AssetRequest) => client.put<AssetResponse>(`/users/${userId}/assets/${id}`, data).then((r) => r.data),
    delete: (userId: number, id: number) => client.delete(`/users/${userId}/assets/${id}`),
};

// Liabilities
export const liabilitiesApi = {
    getAll: (userId: number, type?: LiabilityType) => client.get<LiabilityResponse[]>(`/users/${userId}/liabilities`, { params: type ? { type } : {} }).then((r) => r.data),
    create: (userId: number, data: LiabilityRequest) => client.post<LiabilityResponse>(`/users/${userId}/liabilities`, data).then((r) => r.data),
    update: (userId: number, id: number, data: LiabilityRequest) => client.put<LiabilityResponse>(`/users/${userId}/liabilities/${id}`, data).then((r) => r.data),
    delete: (userId: number, id: number) => client.delete(`/users/${userId}/liabilities/${id}`),
};

// Retirement Accounts
export const retirementApi = {
    getAll: (userId: number) => client.get<RetirementAccountResponse[]>(`/users/${userId}/retirement-accounts`).then((r) => r.data),
    create: (userId: number, data: RetirementAccountRequest) => client.post<RetirementAccountResponse>(`/users/${userId}/retirement-accounts`, data).then((r) => r.data),
    update: (userId: number, id: number, data: RetirementAccountRequest) => client.put<RetirementAccountResponse>(`/users/${userId}/retirement-accounts/${id}`, data).then((r) => r.data),
    delete: (userId: number, id: number) => client.delete(`/users/${userId}/retirement-accoutns/${id}`),
    getProjection: (userId: number, retirementAge = 65) => client.get<ProjectionSummary>(`/uers/${userId}/retirement-projection`, { params: {retirementAge } }).then((r) => r.data),
};

// Income Allocations
export const allocationsApi = {
    getAll: (userId: number) => client.get<IncomeAllocationResponse[]>(`/users/${userId}/income-allocations`).then((r) => r.data),
    create: (userId: number, data: IncomeAllocationRequest) => client.post<IncomeAllocationResponse>(`/users/${userId}/income-allocations`, data).then((r) => r.data),
    update: (userId: number, id: number, data: IncomeAllocationRequest) => client.put<IncomeAllocationResponse>(`/users/${userId}/income-allocations/${id}`, data).then((r) => r.data),
    delete: (userId: number, id: number) => client.delete(`/users/${userId}/income-allocations/${id}`),
};

// Snapshots
export const snapshotsApi = {
    getAll: (userId: number) => client.get<SnapshotResponse[]>(`/users/${userId}/snapshots`).then((r) => r.data),
    getLatest: (userId: number) => client.get<SnapshotResponse>(`/users/${userId}/snapshots/latest`).then((r) => r.data),
    generate: (userId: number) => client.post<SnapshotResponse>(`/users/${userId}/snapshots/generate`).then((r) => r.data),
    delete: (userId: number, id: number) => client.delete(`/users/${userId}/snapshots/${id}`),
};

// Ratios
export const ratiosApi = {
    get: (userid: number) => client.get<RatiosResponse>(`/users/${userid}/ratios`).then((r) => r.data),
};

// Balance Sheet
export const balanceSheetApi = {
    getMonthly: (userId: number, year?: number, month?: number) => client.get<MonthlyBalanceSheet>(`/users/${userId}/balance-sheet/monthly`, { params: { ...(year && { year }), ...(month && { month }) }, }).then((r) => r.data),
    getYearly: (userId: number, year?: number) => client.get<YearlyBalanceSheet>(`/users/${userId}/balance-sheet/yearly`, { params: { ...(year && { year }) }, }).then((r) => r.data),
}