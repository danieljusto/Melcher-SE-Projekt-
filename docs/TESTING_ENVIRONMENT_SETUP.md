# Testing Environment Setup Guide (AI-Agent Optimized)

This guide sets up a complete, repeatable testing environment for the whole repository. It is optimized for an AI coding agent: deterministic, isolated, and fast to rerun.

## 0. Snapshot of this repository (for verification)

- Build system: Maven (`pom.xml`)
- Java version: 17 (Spring Boot 3.2.0 requires Java 17+)
- Test stack: JUnit 5 via `spring-boot-starter-test`
- Database: H2 (default config is file-based with AES cipher under `./data`)
- UI: JavaFX 17.0.2 with OS-specific Maven profiles
- Scheduling: enabled in `com.group_2.Main` via `@EnableScheduling`

## 1. Install and verify the toolchain

1. Install a JDK 17 distribution (Temurin 17 is a safe default).
2. Install Maven 3.9 or newer (required because there is no Maven wrapper in this repo).
3. Verify both are available in your shell:

```bash
java -version
mvn -version
```

Expected: Java reports version 17 and Maven reports 3.9+ with Java 17.

## 2. Confirm you are in the project root

Run this from the repository root (`Melcher-SE-Projekt-`):

```bash
pwd
ls
```

Expected: `pom.xml`, `src/`, and `data/` are present.

## 3. Create a dedicated test profile (isolated database, no background jobs)

The default app config uses a file-based, AES-encrypted H2 database in `./data`. Tests should not touch that file, and they should not run scheduled tasks. Create `src/test/resources/application-test.properties` with the following content:

```properties
spring.datasource.url=jdbc:h2:mem:wgdb_test;DB_CLOSE_DELAY=-1
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=false
spring.h2.console.enabled=false
spring.task.scheduling.enabled=false
```

Why this matters:
- `mem:` keeps the database fully in-memory and isolated per test run.
- `create-drop` makes every test run start cleanly.
- Scheduling is disabled so cron jobs (like standing order processing) do not mutate data during tests.

Optional for extra speed (only if you do not test web endpoints):

```properties
spring.main.web-application-type=none
```

If you later add Spring MVC tests, remove or override that property.

## 4. Create the test source tree

If it does not exist yet, create these directories:

```bash
mkdir -p src/test/java/com/group_2
mkdir -p src/test/resources
```

## 5. Add a baseline "context loads" test

This single test proves the Spring context can start with the test profile:

```java
package com.group_2;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ContextLoadsTest {
    @Test
    void contextLoads() {
    }
}
```

Save it as `src/test/java/com/group_2/ContextLoadsTest.java`.

## 6. Recommended test structure for full coverage

Use a consistent, layer-based layout:

- `src/test/java/com/group_2/service/...` for service tests
- `src/test/java/com/group_2/repository/...` for repository tests
- `src/test/java/com/group_2/util/...` for utility tests

Suggested test types:

- Unit tests: plain JUnit 5 + Mockito (already included via `spring-boot-starter-test`).
- JPA tests: `@DataJpaTest` for repositories, backed by the in-memory H2 config.
- Service integration tests: `@SpringBootTest` with `@ActiveProfiles("test")`.

## 7. Run the tests (AI-friendly commands)

Run everything with the test profile:

```bash
SPRING_PROFILES_ACTIVE=test mvn test
```

Run a single class:

```bash
mvn -Dtest=ContextLoadsTest test
```

Force a clean rebuild and then tests:

```bash
mvn clean test
```

## 8. Optional: split unit vs integration tests

If you want a clean separation between unit tests and slower integration tests, add the Maven Failsafe plugin and use a naming convention like `*IT.java` for integration tests. This keeps `mvn test` fast and uses `mvn verify` for the full suite.

## 9. Optional: JavaFX UI tests (only if you add UI testing)

If you later add UI tests, you will need a JavaFX test framework such as TestFX and (on Linux) the native JavaFX runtime libraries. Keep UI tests isolated and run them in a separate test profile so headless runs stay stable.

## 10. Sanity checklist (quick pass/fail)

1. `java -version` reports 17
2. `mvn -version` reports 3.9+
3. `src/test/resources/application-test.properties` exists and uses `mem:` H2
4. `SPRING_PROFILES_ACTIVE=test mvn test` completes successfully

If any step fails, fix it before moving on to new test work.
