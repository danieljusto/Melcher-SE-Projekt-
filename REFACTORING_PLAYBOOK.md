# Refactoring Playbook

Purpose: keep incremental refactors aligned across architecture, naming, and practices.

## Layering & Dependencies
- Allowed direction: UI (JavaFX controllers) → Service → Repository → Model/Entity. No controller-to-repository or cross-domain calls.
- Cross-domain rules: `core` may be depended on by others; feature domains (`cleaning`, `finance`, `shopping`) should not call each other directly. Shared utilities live in `com.group_2.util`.
- Services are Spring components; repositories stay package-private where possible; controllers should contain only view logic and delegate business logic to services.

## Naming & Structure
- Packages: lowercase domain folders (`core`, `cleaning`, `finance`, `shopping`, `util`).
- Classes: nouns; services end with `Service`, repositories with `Repository`, controllers with `Controller`; DTOs/view models end with `Dto` or `ViewModel`.
- Methods: commands are verbs (`createUser`, `updateRoom`); queries use `get/find/list` prefixes and return data without side effects.
- Constants: use `UPPER_SNAKE_CASE`; enums for closed sets.

## Data Flow & DTOs
- UI layer should exchange DTO/view models; avoid binding JavaFX directly to JPA entities.
- Validate inputs at service boundary; keep entities free of UI concerns.
- Prefer immutability for DTOs and value objects where practical.

## Transactions & Persistence
- Transaction scope lives in services (`@Transactional` on service methods/classes), not controllers.
- Repositories remain thin: derived queries or dedicated methods; no business logic in repositories.

## Null, Validation, Errors
- Avoid returning null from services; use `Optional` for “might be absent” single values or throw domain exceptions for invalid states.
- Validate arguments early; use clear exceptions (`IllegalArgumentException`, domain-specific) with actionable messages.
- Controllers translate domain errors to user feedback; services log at warn/error; controllers generally do not log business events.

## Logging & Monitoring
- Use `slf4j` with structured messages; no `System.out.println` in production paths. Reserve `INFO` for lifecycle, `DEBUG` for details, `WARN/ERROR` for anomalies.

## Configuration & Secrets
- Externalize config in `application.properties`/profiles; avoid hard-coded paths/URLs/credentials in code.

## Testing
- Unit-test services with mocked repositories; integration tests for key flows with in-memory DB when feasible.
- When refactoring, add/adjust tests around changed seams before deeper changes.

## Incremental Refactor Approach
- Work per domain slice (core → cleaning → finance → shopping); finish a slice with naming/structure fixes, dependency cleanup, and tests before moving on.
- Keep changes small and reversible; document decisions and any follow-ups in `REFACTORING_STATUS.md`.
