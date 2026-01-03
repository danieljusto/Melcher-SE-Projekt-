# Refactoring Status

**Last Updated:** 2026-01-03

## Current Overview

### Architecture
| Area                 | Status | Details                                             |
| -------------------- | ------ | --------------------------------------------------- |
| Layered Architecture | ✅      | JavaFX UI → Services → Repositories                 |
| DTO-based API        | ✅      | Finance/Cleaning/Core converted to DTOs             |
| Session Management   | ✅      | Snapshot IDs instead of entity references           |
| Entity Encapsulation | ✅      | `WG` with private fields, LAZY collections          |
| Test Coverage        | ⚠️      | **17 test files** (Service, Repository, Util tests) |

### Remaining Issues

#### 🟠 High
1. **Pending Tests** - More test coverage needed (88 source files)
2. **N+1 Query Problem** - `TransactionService.calculateAllBalances()` performs O(n) DB calls per member
3. **60+ RuntimeException** - No custom exception hierarchy
4. **EAGER Fetch Overuse** - Some entity relations still EAGER

#### 🟡 Medium
5. **Inconsistent @Transactional** - Some query methods have annotation, others don't
6. **Thread Safety** - `SessionManager` singleton with mutable state without synchronization

---

## Completed Refactorings

### Session & DTOs ✅
- `SessionManager` stores only snapshot (IDs + basic data)
- Core View Models: `UserSummaryDTO`, `WgSummaryDTO`, `UserSessionDTO`
- Finance View DTOs: `TransactionViewDTO`, `BalanceViewDTO`, `StandingOrderViewDTO`
- Cleaning DTOs: `CleaningTaskDTO`, `CleaningTaskTemplateDTO`, `RoomDTO`, `WorkingTemplateDTO`

### Finance Domain ✅
- `TransactionService` validates WG membership (creator/creditor/debtors)
- `TransactionHistoryController` consumes View DTOs
- `TransactionsController` uses `BalanceViewDTO`
- Standing Order flows converted to View DTOs
- **Standing Order Cleanup on WG Leave** - `deactivateStandingOrdersForUser()` implemented

### Cleaning Domain ✅
- Cleaning Schedule UI uses DTOs and Session Snapshot IDs
- **`CleaningScheduleService` split into sub-services:**
  - `CleaningTaskAssignmentService` - Task assignment
  - `CleaningTaskLifecycleService` - Task lifecycle
  - `CleaningTemplateService` - Template management
  - `QueueManagementService` - Queue management
  - `RoomService` - Room management

### Core Domain ✅
- `WG` fields private with accessors
- Collections LAZY, id-based `equals/hashCode`
- Member lists retrieved via domain services
- **WG deletion fully implemented** with `deleteByWg()` methods in all repositories

### Utilities ✅
- `FormatUtils` - Currency and date formatting
- `MonthlyScheduleUtil` - Date resolution for templates
- `StringUtils` - String helper methods
- `SplitValidationHelper` - Centralized split validation logic (extracted from controllers)

### Controller Refactoring ✅
- Split validation centralized in `SplitValidationHelper`
- Business logic moved from controllers to services
- Controller audit completed (see `CONTROLLER_AUDIT.md`)

---

## Next Steps (Priority)

### P0 - Critical
```
[ ] Fix N+1 query in calculateAllBalances()
[ ] Create custom exceptions (EntityNotFoundException, ValidationException)
```

### P1 - High
```
[ ] Add more unit tests (increase coverage)
[ ] Improve FinanceMapper null safety
[ ] EAGER → LAZY fetch strategy for remaining relations
```

### P2 - Medium
```
[ ] Inject Clock/Time Provider
[ ] Apply @Transactional consistently (class-level readOnly=true)
[ ] Thread safety for SessionManager
```

### P3 - Low (Optional)
```
[x] Clean up mixed naming (German/English)
[ ] Inject ObjectMapper as bean
[ ] Extend structured logging
```

---

## Known Technical Debt

| Debt               | Risk     | Effort   | Status                        |
| ------------------ | -------- | -------- | ----------------------------- |
| ~~No Tests~~       | ~~High~~ | ~~High~~ | ✅ 17 test files present       |
| N+1 Queries        | High     | Medium   | ⏳ Batch query still pending   |
| Generic Exceptions | Medium   | Medium   | ⏳ Exception hierarchy pending |
| EAGER Fetching     | Medium   | Medium   | ⚠️ Partially converted         |

---

## Security Aspects

| Check                     | Status                                |
| ------------------------- | ------------------------------------- |
| Password Hashing (BCrypt) | ✅                                     |
| Email Uniqueness          | ✅ Optimized                           |
| WG Membership Validation  | ✅ Transactions & Standing Orders      |
| Invite Code Generation    | ⚠️ `Random` instead of `SecureRandom`  |
| Input Validation          | ✅ `SplitValidationHelper` centralized |

---

*See [CONTROLLER_AUDIT.md](./CONTROLLER_AUDIT.md) for controller-specific improvements.*

