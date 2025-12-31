# Refactoring Status

Scope: unbiased snapshot of structural debt and refactor priorities with focus on separation of responsibilities, eliminating redundancy, and aligning to the existing layered rules.

## Current Snapshot
- Layered architecture exists (JavaFX UI -> services -> repositories), but controllers still orchestrate workflows and shape data in multiple domains.
- Finance UI is largely on view DTOs; cleaning UI uses DTOs; other areas mostly rely on DTOs and summary snapshots instead of entities.
- Session boundary is improved (snapshot IDs + summary data), and controllers now use `UserSessionDTO` instead of `getCurrentUser()`.
- Domain entities still expose public state (`WG` fields, EAGER collections), so invariants and membership rules are not enforced consistently.
- Mapping layers still mix access and parsing concerns (notably finance).
- Automated tests are still missing.

## Key Findings
### Architecture and Separation
- Finance controllers still contain dialog construction and UI-specific validation, and some filtering/permission logic remains in the UI.
- Services still mix persistence and view-mapping concerns; no clear application facade layer per domain.
- Navigation/controller hand-offs are repeated (`applicationContext.getBean` after `loadScene`) rather than centralized.
- Session snapshot is used across core/cleaning/finance/shopping controllers, but services still pull from WG entities directly.

### Domain Model and Invariants
- `WG` exposes public fields (`name`, `rooms`, `mitbewohner`) and is accessed directly in services and mappers (e.g., `TransactionService`, `CoreMapper`).
- Authentication remains ad-hoc: `UserService` scans all users with plaintext passwords for login/uniqueness.
- WG membership validation is inconsistent (finance create/update paths still accept debtors/creditors without validating WG membership).

### Finance Domain
- `TransactionService` still uses `wg.mitbewohner` and performs some operations without explicit WG membership checks.
- Settlement/credit-transfer logic is now in the service, but still implemented as multiple transactions without a dedicated domain abstraction for netting.
- `FinanceMapper` performs repository access and JSON parsing inside the mapper and swallows parsing errors.

### Cleaning Domain
- `CleaningScheduleService` remains large and combines scheduling rules, queue maintenance, template handling, and DTO conversion.
- Time handling is still tied to `LocalDate.now()`, limiting testability.

### UI Layer and Redundancy
- Dialog construction, currency formatting, and navigation remain duplicated; encoding issues were partially addressed but not fully standardized.
- UI controllers are mostly DTO-based, but shared dialog/conversion logic is still embedded in controllers.

### Quality and Operations
- No automated tests detected (unit, integration, or UI).
- Global config uses `spring.jpa.hibernate.ddl-auto=update` without profile separation.

## Progress (current iteration)
- Session boundary tightened: `SessionManager` now stores only a snapshot (IDs + basic user/WG data) and provides refresh helpers.
- Core view models added (`UserSummaryDTO`, `WgSummaryDTO`) with a mapper to reduce direct entity exposure.
- Finance view DTOs added (`TransactionViewDTO`, `TransactionSplitViewDTO`, `BalanceViewDTO`) and adopted in finance controllers.
- `TransactionHistoryController` now consumes view DTOs and fetches history via `getTransactionsForUserView`.
- `TransactionsController` now uses `BalanceViewDTO` from `calculateAllBalancesView`.
- Standing order flows moved to view DTOs: services return `StandingOrderViewDTO`, and the dialog uses view-based create/update APIs.
- Settlement and credit transfer moved into `TransactionService` with WG membership validation.
- Cleaning schedule UI uses DTOs and session snapshot IDs for loading tasks.
- Core household setup facade added (`HouseholdSetupService`) to keep core UI controllers from calling cleaning services directly.

## Follow-ups Needed
- Remove remaining direct WG entity field access (`wg.mitbewohner`, `wg.rooms`) in services/mappers; route through repositories or DTOs.
- Add WG membership validation for finance create/update flows; remove direct `wg.mitbewohner` access.
- Introduce application facades per domain (finance, cleaning, shopping) to pull workflow logic out of controllers.
- Split `CleaningScheduleService` into focused services and inject a clock/time provider.
- Encapsulate domain models (`WG` fields, `equals/hashCode`) and enforce invariants.
- Harden authentication (hashing, repository queries, input validation).
- Centralize shared UI utilities (dialogs, currency formatting, navigation) and lock encoding settings.
- Add tests around balances, settlements, cleaning task generation/rotation, and membership rules.
