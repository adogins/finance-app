# Personal Finance App

A full-stack personal finance application for tracking income, expenses, assets, liabilities, retirement savings, and financial health ratios.

---

## Tech Stack

**Backend**
- Java 17 + Spring Boot 3
- MySQL 8.0 (via Docker)
- BCryot password hashing

**Frontend**
- React 19 + TypeScript
- Vite
- Tailwind CSS v3
- Recharts
- Axios
- React Router v6

---

## Project Structure
```
# Verdn — Personal Finance App

A full-stack personal finance application for tracking income, expenses, assets, liabilities, retirement savings, and financial health ratios.

---

## Tech Stack

**Backend**
- Java 17 + Spring Boot 3
- MySQL 8.0 (via Docker)
- BCrypt password hashing

**Frontend**
- React 19 + TypeScript
- Vite
- Tailwind CSS v3
- Recharts
- Axios
- React Router v6

---

## Project Structure

```
finance-app/
│
├── docker-compose.yml
├── docker/
│   └── mysql/
│       └── init.sql
├── README.md
│
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/financeapp/
│       │   │   ├── FinanceApplication.java
│       │   │   ├── config/
│       │   │   │   └── PasswordEncoderConfig.java
│       │   │   ├── controller/
│       │   │   │   ├── UserController.java
│       │   │   │   ├── IncomeController.java
│       │   │   │   ├── ExpenseController.java
│       │   │   │   ├── AssetController.java
│       │   │   │   ├── LiabilityController.java
│       │   │   │   ├── RetirementAccountController.java
│       │   │   │   ├── IncomeAllocationController.java
│       │   │   │   ├── SnapshotController.java
│       │   │   │   ├── RatioController.java
│       │   │   │   ├── RetirementProjectionController.java
│       │   │   │   └── BalanceSheetController.java
│       │   │   ├── dto/
│       │   │   │   ├── UserDto.java
│       │   │   │   ├── IncomeDto.java
│       │   │   │   ├── ExpenseDto.java
│       │   │   │   ├── AssetDto.java
│       │   │   │   ├── LiabilityDto.java
│       │   │   │   ├── RetirementAccountDto.java
│       │   │   │   ├── IncomeAllocationDto.java
│       │   │   │   └── SnapshotDto.java
│       │   │   ├── entity/
│       │   │   │   ├── User.java
│       │   │   │   ├── Income.java
│       │   │   │   ├── Expense.java
│       │   │   │   ├── Asset.java
│       │   │   │   ├── Liability.java
│       │   │   │   ├── RetirementAccount.java
│       │   │   │   ├── IncomeAllocation.java
│       │   │   │   └── Snapshot.java
│       │   │   ├── repository/
│       │   │   │   ├── UserRepository.java
│       │   │   │   ├── IncomeRepository.java
│       │   │   │   ├── ExpenseRepository.java
│       │   │   │   ├── AssetRepository.java
│       │   │   │   ├── LiabilityRepository.java
│       │   │   │   ├── RetirementAccountRepository.java
│       │   │   │   ├── IncomeAllocationRepository.java
│       │   │   │   └── SnapshotRepository.java
│       │   │   └── service/
│       │   │       ├── UserService.java
│       │   │       ├── FinanceRatioService.java
│       │   │       ├── RetirementProjectionService.java
│       │   │       └── BalanceSheetService.java
│       │   └── resources/
│       │       └── application.properties
│       └── test/
│           └── java/com/financeapp/
│               ├── controller/
│               ├── service/
│               └── repository/
│
└── frontend/
    ├── index.html
    ├── package.json
    ├── vite.config.ts
    ├── tailwind.config.ts
    ├── postcss.config.ts
    ├── tsconfig.json
    └── src/
        ├── App.tsx
        ├── main.tsx
        ├── index.css
        ├── api/
        │   └── client.ts                 # Axios client for all endpoints
        ├── context/
        │   ├── AppContext.tsx             # User + toast state
        │   └── AuthContext.tsx            # Auth + localStorage persistence
        ├── components/
        │   ├── charts/
        │   │   ├── BarChart.tsx
        │   │   ├── DonutChart.tsx
        │   │   ├── NetWorthChart.tsx
        │   │   └── RatioCard.tsx
        │   ├── layout/
        │   │   ├── Sidebar.tsx
        │   │   └── TopBar.tsx
        │   └── ui/
        │       ├── Badge.tsx
        │       ├── Button.tsx
        │       ├── Card.tsx
        │       ├── DataTable.tsx
        │       ├── FormField.tsx
        │       ├── Modal.tsx
        │       ├── PageHeader.tsx
        │       ├── Spinner.tsx
        │       ├── StatCard.tsx
        │       └── Toast.tsx
        ├── pages/
        │   ├── AuthPage.tsx
        │   ├── DashboardPage.tsx
        │   ├── IncomePage.tsx
        │   ├── ExpensesPage.tsx
        │   ├── NetWorthPage.tsx
        │   ├── RetirementPage.tsx
        │   ├── BalanceSheetPage.tsx
        │   └── AllocationsPage.tsx
        ├── types/
        │   └── index.ts                  # All DTO types
        └── utils/
            └── format.ts                 # Currency, date, percent formatters
```

---

## Getting Started

### Prerequisites

- Java 17
- Maven
- Node.js 18+
- Docker Desktop

---

### 1. Start the database

```bash
docker-compose up -d
```

This starts MySQL 8.0 on port `3308` and runs `init.sql` to create the schema.

Verify it's running:

```bash
docker ps
```

---

### 2. Start the backend

```bash
mvn spring-boot:run
```

The API will be available at `http://localhost:8080/api`.

---

### 3. Start the frontend

```bash
cd frontend
npm install
npm run dev
```

The app will be available at `http://localhost:5173`.

---

## Features

| Page | Description |
|---|---|
| **Auth** | Sign up and sign in with email and password |
| **Dashboard** | Net worth chart, KPI cards, 6 financial health ratio gauges |
| **Income** | Log and manage income entries, breakdown by source |
| **Expenses** | Log and manage expenses, filter by category, breakdown charts |
| **Net Worth** | Manage assets and liabilities, running net worth total |
| **Retirement** | Retirement account management, projected growth chart with age slider |
| **Balance Sheet** | Monthly and yearly cash flow breakdown |
| **Allocations** | Define income split rules by percentage or fixed amount |

---

## API Overview

All endpoints are prefixed with `/api`. User-owned resources are nested under `/api/users/{userId}/`.

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/users` | Create account |
| `POST` | `/users/login` | Sign in |
| `GET` | `/users/{id}/income` | Get all income |
| `GET` | `/users/{id}/expenses` | Get all expenses |
| `GET` | `/users/{id}/assets` | Get all assets |
| `GET` | `/users/{id}/liabilities` | Get all liabilities |
| `GET` | `/users/{id}/retirement-accounts` | Get retirement accounts |
| `GET` | `/users/{id}/retirement-projection` | Get projected retirement balance |
| `GET` | `/users/{id}/snapshots` | Get net worth snapshots |
| `POST` | `/users/{id}/snapshots/generate` | Generate a new snapshot |
| `GET` | `/users/{id}/ratios` | Get 6 financial health ratios |
| `GET` | `/users/{id}/balance-sheet/monthly` | Get monthly balance sheet |
| `GET` | `/users/{id}/balance-sheet/yearly` | Get yearly balance sheet |
| `GET` | `/users/{id}/income-allocations` | Get income allocation rules |

---

## Database

MySQL 8.0 running in Docker on port `3308`.

| Table | Description |
|---|---|
| `users` | User accounts with bcrypt password hash |
| `income` | Income entries |
| `expenses` | Expense entries with category |
| `assets` | Assets (savings, investments, property, etc.) |
| `liabilities` | Debts with optional interest rate and monthly payment |
| `retirement_accounts` | Retirement accounts with contribution and employer match |
| `income_allocations` | Rules for splitting income by percent or fixed amount |
| `snapshots` | Point-in-time net worth snapshots |

---

## Environment

### Backend — `application.properties`

```properties
spring.datasource.url=jdbc:mysql://localhost:3308/finance_db
spring.datasource.username=financeuser
spring.datasource.password=financepass
```

### Frontend — API base URL

Defined in `src/api/client.ts`:

```ts
const client = axios.create({
  baseURL: 'http://localhost:8080/api',
});
```

---

## Security Notes

- Passwords are hashed with **BCrypt** before storage — plain text passwords are never saved
- The frontend stores the authenticated user object in `localStorage` for session persistence
- This app has no JWT tokens or session management — it is intended for local/personal use only and should not be deployed publicly without adding proper authentication middleware
```

---

## Getting Started

### Prerequisites

- Java 17
- Maven
- Node.js 18+
- Docker Desktop

---

### 1. Start the database

```bash
docker-compose up -d
```

This starts MySQL 8.0 on port `3308` and runs 'init.sql` to create the schema.

Verify it's running:

```bash
docker ps
```

---

### 2. Start the backend

```bash
mvn spring-boot:run
```

Then API will be available at `http://localhost:8080/api`.

---

### 3. Start the frontend

```bash
cd frontend
npm install
npm run dev
```

The app will be available at `http://localhost:5173`.

---

## Features

| Page | Description |
|---|---|
| **Auth** | Sign up and sign in with email and password |
| **Dashboard** | Net worth chart, KPI cards, 6 financial health ratio gauges |
| **Income** | Log and manage income entries, breakdown by source |
| **Expenses** | Log and manage expenses, filter by category, breakdown charts |
| **Net Worth** | Manage assets and liabilities, running net worth total |
| **Retirement** | Retirement account management, projected growth chart with age slider |
| **Balance Sheet** | Monthly and yearly cash flow breakdown |
| **Allocations** | Define income split rules by percentage or fixed amount |

---

## API overview

All endpoints are prefixed with `/api`. User-owned resources are nested under `/api/users/{userId}/`.

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/users` | Create account |
| `POST` | `/users/login` | Sign in |
| `GET` | `/users/{id}/income` | Get all income |
| `GET` | `/users/{id}/expenses` | Get all expenses |
| `GET` | `/users/{id}/assets` | Get all assets |
| `GET` | `/users/{id}/liabilities` | Get all liabilities |
| `GET` | `/users/{id}/retirement-accounts` | Get retirement accounts |
| `GET` | `/users/{id}/retirement-projection` | Get projected retirement balance |
| `GET` | `/users/{id}/snapshots` | Get net worth snapshots |
| `POST` | `/users/{id}/snapshots/generate` | Generate a new snapshot |
| `GET` | `/users/{id}/ratios` | Get 6 financial health ratios |
| `GET` | `/users/{id}/balance-sheet/monthly` | Get monthly balance sheet |
| `GET` | `/users/{id}/balance-sheet/yearly` | Get yearly balance sheet |
| `GET` | `/users/{id}/income-allocations` | Get income allocation rules |

---

## Database

MySQL 8.0 running in Docker on port `3308`

| Table | Description |
|---|---|
| `users` | User accounts with bcrypt password hash |
| `income` | Income entries |
| `expenses` | Expense entries with category |
| `assets` | Assets (savings, investments, property, etc.) |
| `liabilities` | Debts with optional interest rate and monthly payment |
| `retirement_accounts` | Retirement accounts with contribution and employer match |
| `income_allocations` | Rules for splitting income by percent or fixed amount |
| `snapshots` | Point-in-time net worth snapshots |

---

## Environment

### Backend - `application.properties`

```properties
spring.datasource.url=jdbc:mysql://localhost:3308/finance_db
spring.datasource.username=financeuser
spring.datasource.password=financepass
```

### Frontend - API base URL

Defined in `src/api/client.ts`:

```ts
const client = axios.create({
  baseURL: 'http://localhost:8080/api',
});
```

---

## Security Notes
 
- Passwords are hashed with **BCrypt** before storage, plain text passwords are never saved
- The frontend stores the authenticated user object in `localStorage` for session persistence
- This app has no JWT tokens or session management, it is intended for local/personal use only and should not be deployed publicly without adding proper authentication middleware