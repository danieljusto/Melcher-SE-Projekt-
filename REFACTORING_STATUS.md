# Refactoring Status

Scope: tracking incremental cleanup against `REFACTORING_PLAYBOOK.md`.

---

## Open Findings

### UI Redundancies
- [ ] **[MEDIUM]** Inline styles scattered across controllers (100+ `.setStyle()` calls).  
  *Fix:* Extract to CSS classes and apply via `getStyleClass().add()`. Defer to post-DTO phase.

### Architecture
- [x] **[HIGH]** Finance domain DTOs implemented (see Completed section)
- [ ] **[MEDIUM]** Other domains still use direct entity binding (`User`, `Room`, etc.)  
  *Fix:* Extend DTO pattern to other domains as needed.

### Code Quality
- [ ] **[LOW]** Potential duplicate logic in controllers (form validation, dialog setup, etc.).  
  *Fix:* Audit for patterns and extract to utilities or base controller methods.

---

## Next Actions
1. ~~**Centralize alerts**~~ ✅ Done
2. ~~**Implement SLF4J logging**~~ ✅ Done
3. ~~**Introduce DTOs (Finance)**~~ ✅ Done  
4. ~~**Migrate TransactionsController to DTOs**~~ ✅ Done
5. **Migrate remaining finance controllers** – `TransactionHistoryController` and `StandingOrdersDialogController` (complex, deferred)
6. **Extract inline styles** – Move to CSS classes (defer to post-DTO)

---

## Completed
*(Items move here once verified working)*

- [x] FXML files reorganized into domain subfolders (`/core/`, `/cleaning/`, `/finance/`, `/shopping/`).
- [x] `loadScene()` paths updated to reflect new FXML locations.
- [x] Controller `initView()` refactored to parameterless pattern with `SessionManager` injection.
- [x] **[HIGH]** `StandingOrdersDialogController` now uses `UserService.getDisplayName()` instead of calling `UserRepository` directly.
- [x] **[MEDIUM]** `NoWgController` now uses `HouseholdSetupService` instead of `RoomService` (cleaning domain).
- [x] **[MEDIUM]** `SettingsController` now uses `HouseholdSetupService` instead of `RoomService` (cleaning domain).
- [x] **Audit complete** – No remaining layering or cross-domain violations found.
- [x] **[MEDIUM]** Alert dialogs centralized in base `Controller` class with typed methods:
  - `showSuccessAlert()`, `showErrorAlert()`, `showWarningAlert()`, `showConfirmDialog()`
  - Deprecated `showAlert()` methods kept for backward compatibility
  - All controllers updated to use new typed methods
  - Unused `Alert`/`ButtonType` imports removed from refactored controllers
- [x] **[LOW]** SLF4J logging implemented – all `System.out/err.println` calls replaced:
  - `StandingOrderService.java` – 4 calls converted to `log.info()` and `log.error()`
  - `TransactionsController.java` – 2 calls converted to `log.error()` with stack trace
  - `Main.java` – 2 calls converted to `log.info()`
  - Logback provided by Spring Boot starters (no additional dependencies needed)
- [x] **[HIGH]** Finance domain DTO layer implemented:
  - Created `com.group_2.dto.finance` package with:
    - `TransactionDTO` - immutable record for transaction display
    - `TransactionSplitDTO` - immutable record for split details
    - `BalanceDTO` - immutable record for balance display
    - `StandingOrderDTO` - immutable record for standing order display
    - `FinanceMapper` - Spring component for entity-to-DTO conversion
  - Added DTO-returning methods to `TransactionService`:
    - `getTransactionsForUserDTO()`, `getTransactionsByWGDTO()`, `getTransactionByIdDTO()`
    - `calculateAllBalancesDTO()`, `createTransactionDTO()`, `updateTransactionDTO()`
  - Added DTO-returning methods to `StandingOrderService`:
    - `getActiveStandingOrdersDTO()`, `getStandingOrderByIdDTO()`
    - `createStandingOrderDTO()`, `updateStandingOrderDTO()`
  - Success alert added to `TransactionDialogController` on transaction save
- [x] **[MEDIUM]** `TransactionsController` migrated to DTOs:
  - `updateBalanceSheet()` now uses `BalanceDTO` from `TransactionService` decoupling UI from entity logic
  - Refactored `BalanceEntry` (inner class) to store only `userId` instead of full `User` entity
  - Updated settlement logic to resolve `User` from `UserRepository` only when needed

