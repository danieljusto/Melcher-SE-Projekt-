# Refactoring Status

Scope: tracking incremental cleanup against `REFACTORING_PLAYBOOK.md`.

---

## Open Findings

### Domains Not Yet Migrated to DTOs
- [x] ~~Shopping domain~~ Done
- [ ] **[LOW]** `Room` entity still imported in several UI controllers.

### UI Redundancies
- [ ] **[MEDIUM]** Inline styles scattered across controllers (100+ `.setStyle()` calls).  
  *Fix:* Extract to CSS classes and apply via `getStyleClass().add()`.

### Code Quality
- [ ] **[LOW]** Potential duplicate logic in controllers (form validation, dialog setup, etc.).  
  *Fix:* Audit for patterns and extract to utilities or base controller methods.

---

## Next Actions

1. ~~**Migrate Shopping domain to DTOs**~~ Done
   - Created `ShoppingListDTO`, `ShoppingListItemDTO`, `ShoppingMapper`
   - `ShoppingListController` now fully uses DTOs

2. **Remove remaining `Room` entity usage from UI**  
   - `CleaningScheduleController`, `SettingsController`, `NoWgController`, `TemplateEditorController` still use `Room`
   - Create `RoomDTO` or expose room data via existing service methods

3. **Extract inline styles to CSS**  
   - 100+ `.setStyle()` calls scattered across controllers
   - Move to `styles.css` using `.getStyleClass().add()`

4. **Audit duplicate logic in controllers**  
   - Form validation, dialog setup patterns appear repeated
   - Extract to utilities or base `Controller` methods

5. **(Optional) Create `UserDTO` / `WgDTO`**  
   - `User` and `WG` imports remain in most controllers (used for session/auth context)
   - Lower priority since these are core identity objects

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
- [x] **[HIGH]** `StandingOrdersDialogController` migrated to DTOs:
  - Refactored to use `StandingOrderDTO` for table display
  - Added ID-based `create`/`update` methods to `StandingOrderService` to fully decouple controller from entity instantiation
  - Fixed multiple null-safety and create/update logic issues during migration
- [x] **[MEDIUM]** `TransactionHistoryController` migrated to DTOs:
  - Refactored to fully utilize `TransactionDTO` and `TransactionSplitDTO`
  - Updated TableView columns and filter logic to consume immutable records
  - Decoupled dialog logic from Entity classes
- [x] **[MEDIUM]** Finance controller create/settlement paths now use DTO services:
  - `TransactionDialogController` uses `createTransactionDTO`/`createStandingOrderDTO`
  - `TransactionsController` settlement and credit-transfer flows use `createTransactionDTO`
- [x] **[LOW]** Finance controllers now use UserService (no repo in UI)
- [x] **[MEDIUM]** Cleaning schedule migrated to DTOs:
  - Added `CleaningTaskDTO` and `CleaningMapper`
  - `CleaningScheduleService` exposes DTO getters and id-based update/reassign/reschedule helpers
  - `CleaningScheduleController` consumes DTOs (no direct `CleaningTask` manipulation)
- [x] **[MEDIUM]** `TemplateEditorController` migrated to DTOs:
  - Created `CleaningTaskTemplateDTO` for template display
  - Added `getTemplatesDTO()` and `addTemplateByRoomId()` to service
  - Refactored `WorkingTemplate` inner class to use IDs instead of entity references
- [x] **[MEDIUM]** Shopping domain migrated to DTOs:
  - Created `com.group_2.dto.shopping` package with `ShoppingListDTO`, `ShoppingListItemDTO`, `ShoppingMapper`
  - Added ID-based service methods: `getAccessibleListsDTO()`, `createListByUserIds()`, `addItemByIds()`, etc.
  - `ShoppingListController` fully refactored to use DTOs (no direct entity binding)
