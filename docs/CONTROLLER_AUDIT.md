# Controller Audit: Open Improvements

**Last Updated:** 2026-01-03  

---

## 🟡 Open Cases (Optional)

### 1. TransactionsController (614 lines)

| Lines   | Method                   | Issue                                      | Recommendation         |
| ------- | ------------------------ | ------------------------------------------ | ---------------------- |
| 163-187 | `updateBalanceDisplay()` | Card color based on balance classification | Move to Service or DTO |
| 313-338 | `showSettlementDialog()` | Filter available credits for transfer      | Move to Service        |

### 2. TransactionHistoryController (756 lines)

| Lines   | Method              | Issue                                                 | Recommendation                    |
| ------- | ------------------- | ----------------------------------------------------- | --------------------------------- |
| 264-322 | `populateFilters()` | Year generation, sorting, member lookup               | Move to Service                   |
| 324-383 | `applyFilters()`    | Complex filter logic (Year/Month/Payer/Debtor/Search) | Create `TransactionFilterService` |

### 3. TransactionDialogController (981 lines) ⚠️ **Largest Controller**

| Lines   | Method                            | Issue                                          | Recommendation                                  |
| ------- | --------------------------------- | ---------------------------------------------- | ----------------------------------------------- |
| 400-452 | `buildStandingOrderDescription()` | Complex string building with date calculations | Move to `StandingOrderService` or `FormatUtils` |

### 4. StandingOrdersDialogController (730 lines)

| Lines   | Method               | Issue                                     | Recommendation                |
| ------- | -------------------- | ----------------------------------------- | ----------------------------- |
| 169-187 | `formatFrequency()`  | Switch statement for frequency formatting | Move to Enum or `FormatUtils` |
| 189-201 | `parseDebtorNames()` | Stream-based string construction          | Move to Mapper                |

### 5. NoWgController (224 lines)

| Lines   | Method             | Issue                                  | Recommendation                                |
| ------- | ------------------ | -------------------------------------- | --------------------------------------------- |
| 155-164 | `handleCreateWg()` | Loop creating rooms and collecting IDs | Move transaction handling entirely to Service |

### 6. SettingsController / ProfileController

| Controller         | Issue                                    | Recommendation                    |
| ------------------ | ---------------------------------------- | --------------------------------- |
| SettingsController | `isCurrentUserAdmin()` permission check  | Create `PermissionService` or DTO |
| ProfileController  | Email validation (`email.contains("@")`) | Create `ValidationService`        |

---

## 🟢 Clean Controllers (no action needed)

| Controller                   | Lines |
| ---------------------------- | ----- |
| `LoginController`            | 77    |
| `SignUpController`           | 79    |
| `MainScreenController`       | 115   |
| `NavbarController`           | 71    |
| `ShoppingListController`     | 525   |
| `CleaningScheduleController` | 747   |
| `TemplateEditorController`   | 602   |

---

## 🎯 Optional Improvements

1. **`PermissionService`** - Unified permission checks
2. **`ValidationService`** - Email validation and input checks
3. **`TransactionFilterService`** - Filter logic from TransactionHistoryController
4. **`TransactionDialogState`** extension - Business validation

---

*All critical refactorings have been completed. The remaining items are optional.*
