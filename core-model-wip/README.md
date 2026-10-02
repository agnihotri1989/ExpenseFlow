# Multi-Module Migration Attempt (Parked / Incomplete)

## Status: Incomplete Multi-Module Architecture
This directory documents an incomplete architectural migration from a single-module monolithic structure (`:app`) to a multi-module architecture:

- Target Modules:
  - `:core:model` (Domain entities: `Expense`, `Category`, `TransactionType`)
  - `:core:database` (Room persistence & migrations)
  - `:feature:analytics` (Compose charts and reporting)
- Current State:
  - Attempt parked due to tight coupling with legacy Java/XML screens (`AddExpenseActivity.java`, `ExpenseHistoryActivity.java`).
  - All functional code is consolidated inside the single `:app` module to guarantee runtime stability and build compatibility.
