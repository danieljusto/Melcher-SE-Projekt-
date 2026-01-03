# To Dos

## 🔴 Critical (Priority 0)
- [ ] Fix N+1 Query Problem in `TransactionService.calculateAllBalances()`
- [ ] Create custom exception hierarchy (`EntityNotFoundException`, `UnauthorizedOperationException`, `ValidationException`)

## 🟠 High (Priority 1)
- [ ] Add more unit tests (currently 17 test files for 88 source files)
- [ ] Improve null safety in `FinanceMapper` (null-checks before nested object access)
- [ ] EAGER → LAZY fetch strategy for remaining entity relations

## 🟡 Medium (Priority 2)
- [ ] Inject Clock/Time Provider into services
- [ ] Apply `@Transactional` consistently (class-level `readOnly=true`)
- [ ] Thread safety for `SessionManager` (singleton with mutable state)
- [ ] Optional: Move more controller workflow logic to services (see `CONTROLLER_AUDIT.md`)

## ✅ Completed

### Refactoring
- [x] Converted Finance controllers to View DTOs
- [x] Implemented session boundary with snapshot IDs
- [x] Encapsulated `WG` fields (private + accessors, LAZY collections)
- [x] Transaction create/update validates WG membership
- [x] Split `CleaningScheduleService` into sub-services:
  - `CleaningTaskAssignmentService`
  - `CleaningTaskLifecycleService`
  - `CleaningTemplateService`
  - `QueueManagementService`
  - `RoomService`
- [x] Created `SplitValidationHelper` for centralized split validation
- [x] Created `WorkingTemplateDTO` for Template Editor
- [x] Created `FormatUtils`, `MonthlyScheduleUtil`, `StringUtils` utilities
- [x] Cleaned up mixed naming (German/English) in documentation

### WG Management
- [x] Fully implemented WG deletion (all `deleteByWg` methods)
- [x] Standing order cleanup on WG leave (`deactivateStandingOrdersForUser()`)

### Testing
- [x] Added unit tests (17 test files)
  - Service Tests: `UserServiceTest`, `WGServiceTest`, `TransactionServiceTest`, `CleaningScheduleServiceTest`, `ShoppingListServiceTest`
  - Repository Tests: `UserRepositoryTest`, `WGRepositoryTest`, `TransactionRepositoryTest`, `CleaningTaskRepositoryTest`, `RoomRepositoryTest`, `ShoppingListRepositoryTest`
  - Util Tests: `FormatUtilsTest`, `MonthlyScheduleUtilTest`
  - Model Tests: `RoomAssignmentQueueTest`

### Security
- [x] Implemented password hashing with BCrypt
- [x] WG membership validation in Standing Order create/update

## 📋 Backlog (Optional)

### Features
- [ ] Block leaving WG with negative balance
- [ ] Improve wording for single-debtor transactions
- [ ] Transaction History: Display transactions of former members
- [ ] Notifications for Cleaning Schedule and transactions

### UI/Style
- [ ] Improve icons and dialog styling

### Security
- [ ] Use `SecureRandom` instead of `Random` for invite codes
- [ ] Admin override for content moderation

### Testing
- [ ] Integration tests for critical workflows

### Extras
- [ ] Settings: Currency selection
- [ ] Shopping List: Payment link (optional)
