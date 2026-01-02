# Repository Testing Guide (Step by Step)

This guide is a detailed, repeatable workflow to create and run tests for the whole repository. It assumes you already followed the environment setup in `TESTING_ENVIRONMENT_SETUP.md`.

## 0. Preconditions (do this once)

1. Confirm the test profile file exists:
   - `src/test/resources/application-test.properties`
2. Confirm the test source tree exists:
   - `src/test/java/com/group_2`
3. Run a smoke test to verify the environment:

```bash
SPRING_PROFILES_ACTIVE=test mvn test
```

If this fails, stop and fix the environment first.

## 1. Build a coverage map of the repository

Use this list to ensure every functional area gets tests:

- Core: `com.group_2.service.core`
  - `UserService`, `WGService`, `HouseholdSetupService`, `DatabaseCleanupService`, `PasswordEncryptionService`, `CoreViewService`
- Cleaning: `com.group_2.service.cleaning`
  - `CleaningScheduleService`, `CleaningTaskLifecycleService`, `CleaningTemplateService`, `CleaningTaskAssignmentService`, `QueueManagementService`, `RoomService`
- Finance: `com.group_2.service.finance`
  - `TransactionService`, `StandingOrderService`
- Shopping: `com.group_2.service.shopping`
  - `ShoppingListService`
- Repositories: `com.group_2.repository` and subpackages
  - `UserRepository`, `WGRepository`, `ShoppingListRepository`, `ShoppingListItemRepository`
  - `TransactionRepository`, `StandingOrderRepository`, `TransactionSplitRepository`
  - `CleaningTaskRepository`, `CleaningTaskTemplateRepository`, `RoomRepository`, `RoomAssignmentQueueRepository`
- Utilities: `com.group_2.util`
  - `FormatUtils`, `MonthlyScheduleUtil`, `SessionManager`
- UI controllers: `com.group_2.ui.*` (optional, see Step 7)

## 2. Decide the test types per layer

Use this as your default decision tree:

- Repository tests: `@DataJpaTest` (fast, isolated, H2 in-memory).
- Service tests: `@SpringBootTest` with `@ActiveProfiles("test")` (real wiring).
- Utility tests: plain JUnit (no Spring context).
- UI controller tests: unit tests with mocked services, no JavaFX runtime (optional).

## 3. Create test support utilities (do this once)

Create a reusable test support package:

```
src/test/java/com/group_2/testsupport
```

Recommended helper classes:

- `TestDataFactory` for common entities (WG, User, Room, Transaction, ShoppingList).
- `TestClock` (optional) if you need deterministic dates.
- `TestSession` helper to reset `SessionManager` state between tests.

Minimal example structure:

```java
package com.group_2.testsupport;

import com.group_2.model.User;
import com.group_2.model.WG;

public final class TestDataFactory {
    private TestDataFactory() {}

    public static WG wg(String name) {
        WG wg = new WG();
        wg.setName(name);
        return wg;
    }

    public static User user(String email, WG wg) {
        User user = new User();
        user.setEmail(email);
        user.setWg(wg);
        return user;
    }
}
```

Keep these helpers small and focused to avoid building huge object graphs.

## 4. Repository tests (one repository at a time)

Pattern:

```java
package com.group_2.repository;

import com.group_2.model.User;
import com.group_2.model.WG;
import com.group_2.testsupport.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    void savesAndFindsByEmail() {
        WG wg = TestDataFactory.wg("Test WG");
        User user = TestDataFactory.user("user@example.com", wg);

        userRepository.save(user);

        assertThat(userRepository.findAll())
            .anyMatch(saved -> "user@example.com".equals(saved.getEmail()));
    }
}
```

Recommended repository test checklist:

- save and retrieve
- custom query methods (if any)
- relationships persist as expected

## 5. Service tests (focus on business rules)

Pattern:

```java
package com.group_2.service.core;

import com.group_2.model.User;
import com.group_2.model.WG;
import com.group_2.repository.UserRepository;
import com.group_2.repository.WGRepository;
import com.group_2.testsupport.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class UserServiceTest {
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private WGRepository wgRepository;

    @Test
    void registersUser() {
        WG wg = wgRepository.save(TestDataFactory.wg("WG A"));
        User user = TestDataFactory.user("new@example.com", wg);

        User saved = userService.registerUser(user);

        assertThat(saved.getId()).isNotNull();
        assertThat(userRepository.findAll()).hasSize(1);
    }
}
```

Service test checklist (by module):

- Core: user registration, authentication, WG creation/join/leave
- Cleaning: task creation, assignment rotation, schedule generation
- Finance: transaction creation, splits, balance calculations, standing orders
- Shopping: list creation, item add/remove, completion state

## 6. Scheduling tests (only where needed)

Scheduling is disabled by default in the test profile. If you must test the scheduled logic in `StandingOrderService`, call the method directly instead of relying on the scheduler.

If you need to test the scheduler wiring:

```java
@SpringBootTest(properties = "spring.task.scheduling.enabled=true")
@ActiveProfiles("test")
class StandingOrderSchedulerTest { ... }
```

Keep these tests minimal to avoid flaky time-based failures.

## 7. UI controller tests (optional)

These controllers are JavaFX components and are best tested without loading FXML:

- Mock the services they depend on.
- Instantiate the controller directly.
- Call handler methods that do not require actual JavaFX runtime.

If UI tests become necessary, keep them in a separate profile and run them explicitly, so normal test runs remain fast and stable.

## 8. Deterministic data and cleanup

Use one of these patterns for isolation:

- `@DataJpaTest` (transaction rollback by default)
- `@SpringBootTest` with `@Transactional`
- `@DirtiesContext` only when state is truly global

If `SessionManager` holds state across tests, reset it in `@BeforeEach`.

## 9. Run the full repository test suite

Standard run:

```bash
SPRING_PROFILES_ACTIVE=test mvn test
```

Run a single test class:

```bash
mvn -Dtest=UserServiceTest test
```

Run a subset by naming pattern (for large suites):

```bash
mvn -Dtest=*ServiceTest test
```

## 10. AI-agent workflow (fast and safe)

Use this loop for each module:

1. Write or update one test class.
2. Run only that class (`-Dtest=...`).
3. Fix failures immediately.
4. Move to the next class once green.

This keeps test feedback tight and avoids long full-suite cycles.

## 11. Troubleshooting guide

- H2 file lock or missing data: verify you are using the in-memory test profile.
- Scheduler side effects: confirm `spring.task.scheduling.enabled=false`.
- JavaFX errors during tests: avoid `@SpringBootTest` for UI, or keep UI tests isolated.
- Random test failures: remove time-based assertions or use fixed timestamps.

## 12. Completion checklist

- Every service has at least one core behavior test.
- Every repository has at least one persistence test.
- Utilities have direct unit tests.
- Full suite passes with `SPRING_PROFILES_ACTIVE=test mvn test`.

When this checklist is green, the repository test coverage is functionally complete and stable for continuous AI-driven changes.
